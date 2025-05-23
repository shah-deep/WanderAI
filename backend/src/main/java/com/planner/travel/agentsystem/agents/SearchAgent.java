package com.planner.travel.agentsystem.agents;

import com.planner.travel.agentsystem.assistant.SearchAssistant;
import com.planner.travel.agentsystem.state.ChatState;
import com.planner.travel.agentsystem.tools.LocationSearchTool;
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
public class SearchAgent implements NodeAction<ChatState> {

    private static final Logger logger = Logger.getLogger(SearchAgent.class.getName());

    private final String llmApiKey;
    private final String llmModel;
    private final SearchAssistant service;

    public SearchAgent(String llmApiKey, String llmModel, String tripAdvisorApiKey, TripAdvisorClient tripAdvisorClient) {
        this.llmApiKey = llmApiKey;
        this.llmModel = llmModel;
        
        // Create a new instance of the tool for this agent
        LocationSearchTool locationSearchTool = new LocationSearchTool(tripAdvisorClient, new ObjectMapper(), tripAdvisorApiKey);
        // Create the assistant with the tool
        this.service = build(locationSearchTool);
    }

    private SearchAssistant build(LocationSearchTool locationSearchTool) {
        ChatModel llm = GoogleAiGeminiChatModel.builder()
                .apiKey(llmApiKey)
                .modelName(llmModel)
                .temperature(0.0)
                .build();

        return AiServices.builder(SearchAssistant.class)
                .chatModel(llm)
                .tools(locationSearchTool)
                .build();
    }

    @Override
    public Map<String, Object> apply(ChatState state) throws Exception {
        String result;
        try {
            var message = state.lastMessage().orElseThrow();

            var text = switch (message.type()) {
                case USER -> ((UserMessage) message).singleText();
                case AI -> ((AiMessage) message).text();
                default -> throw new IllegalStateException("unexpected message type: " + message.type());
            };

             // System.out.println("SearchAgent got input: " + text);

            result = service.chat(text);
            if (result == null || result.isEmpty()) {
                throw new Exception("Failed to get response from search assistant");
            }
             // System.out.println("SearchAgent got output: " + result);
        } catch (Exception e) {
            logger.warning("Error occurred during search: " + e.getMessage());

            if (e instanceof TimeoutException) {
                result = "I'm sorry, but the search operation timed out. Please try again or refine your search query.";
            } else {
                result = "Search Agent couldn't find any results matching the search criteria. Please try with different keywords being more specific and do not repeat the same query. It is possible that the user query is incorrect.";
            }
            
            // logger.warning("Returning fallback response: " + result);
        }

        return Map.of("messages", AiMessage.from(result));
    }
}