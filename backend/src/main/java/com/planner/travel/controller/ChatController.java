package com.planner.travel.controller;

import com.planner.travel.agentsystem.TravelPlannerWorkflow;
import com.planner.travel.agentsystem.state.ChatState;
import com.planner.travel.dto.ChatMessageDto;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
//import org.bsc.langgraph4j.StateGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final TravelPlannerWorkflow travelPlannerWorkflow;
    private final Map<String, CompiledGraph<ChatState>> sessionGraphs = new ConcurrentHashMap<>();
    private final Map<String, Optional<ChatState>> sessionChatStates = new ConcurrentHashMap<>();


    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, TravelPlannerWorkflow travelPlannerWorkflow) {
        this.messagingTemplate = messagingTemplate;
        this.travelPlannerWorkflow = travelPlannerWorkflow;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        if (sessionId == null) {
            logger.error("Session ID is null, cannot process message");
            // Optionally, send an error back to a generic error topic or handle
            return;
        }

        try {
            CompiledGraph<ChatState> graph = sessionGraphs.computeIfAbsent(sessionId, id -> {
                try {
                    logger.info("Creating new StateGraph for session: {}", id);
                    return travelPlannerWorkflow.createStateGraph().compile();
                } catch (GraphStateException e) {
                    logger.error("Error creating StateGraph for session {}: {}", id, e.getMessage(), e);
                    throw new RuntimeException("Could not create agent graph", e);
                }
            });

            Optional<ChatState> currentChatState = sessionChatStates.computeIfAbsent(sessionId, id ->
                    Optional.of(new ChatState(Map.of("messages", Collections.emptyList())))
            );

            UserMessage userMessage = UserMessage.from(chatMessageDto.getContent());
            ChatState updatedChatStateWithUserMessage;
            if (currentChatState.isEmpty()) {
                logger.warn("No chat state found for session {}", sessionId);
                updatedChatStateWithUserMessage = new ChatState(Map.of("messages", Collections.emptyList()));
                updatedChatStateWithUserMessage = updatedChatStateWithUserMessage.withMessage(userMessage);
            } else {
                updatedChatStateWithUserMessage = currentChatState.get().withMessage(userMessage);
            }

            logger.info("Invoking graph for session: {} with message: {}", sessionId, chatMessageDto.getContent());
            Optional<ChatState> resultState = graph.invoke(updatedChatStateWithUserMessage.data());
            sessionChatStates.put(sessionId, resultState); // Store the latest state

            if (resultState.isPresent() && resultState.get().lastMessage().isPresent() && 
                (resultState.get().lastMessage().get() instanceof AiMessage aiResponse)) {
            
                ChatMessageDto responseDto = new ChatMessageDto(aiResponse.text(), "AI");
                logger.info("Sending AI response to session {}: {}", sessionId, responseDto.getContent());
            
                // Use a simpler approach with a session-specific topic
                messagingTemplate.convertAndSend("/topic/reply/" + sessionId, responseDto);
                logger.info("Message Sent!");
            } else {
                logger.warn("No AI message found in result state for session {}", sessionId);
            
                if (resultState.isPresent() && !resultState.get().messages().isEmpty()) {
                    ChatMessage lastMessage = resultState.get().lastMessage().get();
                    if (lastMessage instanceof AiMessage) {
                        ChatMessageDto responseDto = new ChatMessageDto(((AiMessage) lastMessage).text(), "AI");
                        messagingTemplate.convertAndSend("/topic/reply/" + sessionId, responseDto);
                    } else {
                        System.out.println(lastMessage);
                        logger.warn("Last message for session {} was not an AI Message: {}", sessionId, lastMessage.type());
                    }
                } else {
                    ChatMessageDto errorDto = new ChatMessageDto("Sorry, I couldn't process that.", "AI");
                    messagingTemplate.convertAndSend("/topic/reply/" + sessionId, errorDto);
                }
            }

        } catch (Exception e) {
            logger.error("Error processing message for session {}: {}", sessionId, e.getMessage(), e);
            ChatMessageDto errorDto = new ChatMessageDto("Sorry, an error occurred: " + e.getMessage(), "AI");
            messagingTemplate.convertAndSend("/topic/reply/" + sessionId, errorDto);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        logger.info("Session disconnected: {}", sessionId);
        sessionGraphs.remove(sessionId);
        sessionChatStates.remove(sessionId);
        logger.info("Cleaned up resources for session: {}", sessionId);
    }
}