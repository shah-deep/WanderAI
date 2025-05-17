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

import jakarta.annotation.PostConstruct; // Import PostConstruct
import java.util.Map;


@Service
@RequiredArgsConstructor
public class SearchAgent implements NodeAction<ChatState> {

    private final LocationSearchTool locationSearchTool;

    @Value( "${llm.api-key.gemini}")
    private String llmApiKey;

    @Value( "${llm.model-name.gemini}")
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

        var text = switch( message.type() ) {
            case USER -> ((UserMessage)message).singleText();
            case AI -> ((AiMessage)message).text();
            default -> throw new IllegalStateException("unexpected message type: " + message.type() );
        };

        System.out.println("SearchAgent got input: " + text);

        var result = service.chat(text);

        System.out.println("SearchAgent got output: " + result);

        return Map.of( "messages", AiMessage.from(result) );
    }

}