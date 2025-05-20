package com.planner.travel.agentsystem.agents;

import com.planner.travel.agentsystem.assistant.SearchAssistant;
import com.planner.travel.agentsystem.state.ChatState;
import com.planner.travel.agentsystem.tools.LocationSearchTool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;


@Service
@RequiredArgsConstructor
public class SearchAgent implements NodeAction<ChatState> {

    private final LocationSearchTool locationSearchTool;
    private static final Logger logger = Logger.getLogger(SearchAgent.class.getName());

    @Value("${llm.api-key.gemini}")
    private String llmApiKey;

    @Value("${llm.model-name.gemini}")
    private String llmModel;

    private SearchAssistant service;

    @PostConstruct
    public void init() {
        this.service = build();
    }

    SearchAssistant build() {
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
        var message = state.lastMessage().orElseThrow();

        var text = switch(message.type()) {
            case USER -> ((UserMessage)message).singleText();
            case AI -> ((AiMessage)message).text();
            default -> throw new IllegalStateException("unexpected message type: " + message.type());
        };

        System.out.println("SearchAgent got input: " + text);

        String result;
        try {
            result = service.chat(text);
            System.out.println("SearchAgent got output: " + result);
        } catch (Exception e) {
            logger.warning("Error occurred during search: " + e.getMessage());
            
            if (e instanceof TimeoutException) {
                result = "I'm sorry, but the search operation timed out. Please try again or refine your search query.";
            } else if (e.getMessage() != null && e.getMessage().contains("No results found")) {
                result = "I couldn't find any results matching your search criteria. Please try with different keywords or a broader search term.";
            } else {
                result = "I encountered an issue while searching for locations. Please try again with a different query.";
            }
            
            logger.warning("Returning fallback response: " + result);
        }

        return Map.of("messages", AiMessage.from(result));
    }
}