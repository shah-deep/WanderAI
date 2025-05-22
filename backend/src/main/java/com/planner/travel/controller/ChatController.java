package com.planner.travel.controller;

import com.planner.travel.agentsystem.TravelPlannerWorkflow;
import com.planner.travel.agentsystem.state.ChatState;
import com.planner.travel.dto.ChatMessageDto;
import com.planner.travel.util.ErrorHandler;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.RunnableConfig;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final TravelPlannerWorkflow travelPlannerWorkflow;
    
    // Store memory savers per session
    private final Map<String, MemorySaver> sessionMemory = new ConcurrentHashMap<>();
    private final Map<String, CompiledGraph<ChatState>> sessionGraphs = new ConcurrentHashMap<>();

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
            return;
        }

        try {
            // Get or create the memory saver for this session
            MemorySaver memorySaver = sessionMemory.computeIfAbsent(sessionId, id -> new MemorySaver());
            
            // Get or create the compiled graph with memory configuration
            CompiledGraph<ChatState> graph = sessionGraphs.computeIfAbsent(sessionId, id -> {
                try {
                    logger.info("Creating new StateGraph for session: {}", id);
                    CompileConfig compileConfig = CompileConfig.builder()
                            .checkpointSaver(memorySaver)
                            .build();
                    return travelPlannerWorkflow.createStateGraph().compile(compileConfig);
                } catch (GraphStateException e) {
                    logger.error("Error creating StateGraph for session {}: {}", id, e.getMessage(), e);
                    throw new RuntimeException("Could not create agent graph", e);
                }
            });

            logger.info("Invoking graph for session: {} with message: {}", sessionId, chatMessageDto.getContent());
            
            // Create runnable config with session ID as thread ID
            RunnableConfig runnableConfig = RunnableConfig.builder()
                    .threadId(sessionId)
                    .build();
            
            // Invoke the graph with the new message and runnable config
            Optional<ChatState> resultState = graph.invoke(
                Map.of("messages", UserMessage.from(chatMessageDto.getContent())), 
                runnableConfig
            );

            if (resultState.isPresent() && resultState.get().lastMessage().isPresent() && 
                (resultState.get().lastMessage().get() instanceof AiMessage aiResponse)) {
            
                ChatMessageDto responseDto = new ChatMessageDto(aiResponse.text(), "AI");
                logger.info("Sending AI response to session {}: {}", sessionId, responseDto.getContent());
            
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
            ChatMessageDto errorDto = new ChatMessageDto(
                ErrorHandler.getUserFriendlyErrorMessage(e, "Error processing chat message", true),
                "AI"
            );
            messagingTemplate.convertAndSend("/topic/reply/" + sessionId, errorDto);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        logger.info("Session disconnected: {}", sessionId);
        
        // Clean up both graph and memory resources
        sessionGraphs.remove(sessionId);
        sessionMemory.remove(sessionId);
        
        logger.info("Cleaned up resources for session: {}", sessionId);
    }
}