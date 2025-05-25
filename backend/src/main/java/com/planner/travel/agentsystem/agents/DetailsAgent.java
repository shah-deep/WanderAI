package com.planner.travel.agentsystem.agents;

import com.planner.travel.agentsystem.assistant.DetailsAssistant;
import com.planner.travel.agentsystem.state.ChatState;
import com.planner.travel.agentsystem.tools.LocationDetailsTool;
import com.planner.travel.client.TripAdvisorClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class DetailsAgent implements NodeAction<ChatState> {

    private static final Logger logger = Logger.getLogger(DetailsAgent.class.getName());

    private final String llmApiKey;
    private final String llmModel;
    private final DetailsAssistant service;

    public DetailsAgent(String llmApiKey, String llmModel, String tripAdvisorApiKey, TripAdvisorClient tripAdvisorClient) {
        this.llmApiKey = llmApiKey;
        this.llmModel = llmModel;

        LocationDetailsTool locationDetailsTool = new LocationDetailsTool(tripAdvisorClient, new ObjectMapper(), tripAdvisorApiKey);
        this.service = build(locationDetailsTool);
    }

    private DetailsAssistant build(LocationDetailsTool locationDetailsTool) {
        ChatModel llm = GoogleAiGeminiChatModel.builder()
                .apiKey(llmApiKey)
                .modelName(llmModel)
                .temperature(0.0)
                .build();

        return AiServices.builder(DetailsAssistant.class)
                .chatModel(llm)
                .tools(locationDetailsTool)
                .build();
    }

    @Override
    public Map<String, Object> apply(ChatState state) throws Exception {
        String result;
        try {
            var message = state.lastMessage().orElseThrow();

            var text = switch(message.type()) {
                case USER -> ((UserMessage)message).singleText();
                case AI -> ((AiMessage)message).text();
                default -> throw new IllegalStateException("unexpected message type: " + message.type());
            };

             // System.out.println("DetailsAgent got input: " + text);

            result = service.chat(text);
            if (result == null || result.isEmpty()) {
                throw new Exception("Failed to get response from details assistant");
            }
             // System.out.println("DetailsAgent got output: " + result);
        } catch (Exception e) {
            logger.warning("Error occurred during location details retrieval: " + e.getMessage());
            
            if (e instanceof TimeoutException) {
                result = "I'm sorry, but the location details request timed out. Please try again or refine your query.";
            } else {
                result = "Details Agent couldn't retrieve the requested location information. Please try with different parameters or check if the location exists.";
            }
            
            // logger.warning("Returning fallback response: " + result);
        }

        return Map.of("messages", AiMessage.from(result));
    }
}