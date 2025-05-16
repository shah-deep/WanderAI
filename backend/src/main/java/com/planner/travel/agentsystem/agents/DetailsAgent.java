package com.planner.travel.agentsystem.agents;

import com.planner.travel.agentsystem.assistant.DetailsAssistant;
import com.planner.travel.agentsystem.state.ChatState;
import com.planner.travel.agentsystem.tools.LocationDetailsTool;
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
public class DetailsAgent implements NodeAction<ChatState> {

    private final LocationDetailsTool locationDetailsTool;

    @Value( "${llm.api-key.gemini}")
    private String llmApiKey;

    private DetailsAssistant service;

    @PostConstruct
    public void init() {
        this.service = build();
    }

    public DetailsAssistant build() {
        ChatModel llm = GoogleAiGeminiChatModel.builder()
                .apiKey(llmApiKey)
                .modelName("gemini-1.5-flash")
                .temperature(0.0) // Adjust as needed
                .build();

        return AiServices.builder(DetailsAssistant.class)
                .chatModel(llm)
                .tools(locationDetailsTool)
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

        System.out.println("DetailsAgent got input: " + text);

        var result = service.chat(text);

        System.out.println("DetailsAgent got output: " + result);

        return Map.of( "messages", AiMessage.from(result) );
    }
}