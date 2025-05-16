package com.planner.travel.agentsystem.agents;

import com.planner.travel.agentsystem.assistant.SupervisorAssistant;
import com.planner.travel.agentsystem.responseformat.SupervisorOutput;
import com.planner.travel.agentsystem.state.ChatState;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.output.JsonSchemas;
import lombok.RequiredArgsConstructor; // If you use Lombok
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct; // Import PostConstruct
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupervisorAgent implements NodeAction<ChatState> {

    @Value("${llm.api-key.gemini}")
    private String llmApiKey;

    private SupervisorAssistant service;

    @PostConstruct
    public void init() {
        this.service = buildSupervisorAssistant();
    }

    public SupervisorAssistant buildSupervisorAssistant() {
        // 1. Generate JsonSchema from your SupervisorOutput class
        JsonSchema supervisorOutputSchema = JsonSchemas.jsonSchemaFrom(SupervisorOutput.class)
                .orElseThrow(() -> new RuntimeException("Failed to create JSON schema from SupervisorOutput class. Ensure SupervisorOutput has a public no-arg constructor and getters/setters if needed by the schema generator."));

        // 2. Define the ResponseFormat to be JSON, using the generated schema
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(supervisorOutputSchema)
                .build();

        // 3. Configure the ChatModel (Gemini)
        ChatModel llm = GoogleAiGeminiChatModel.builder()
                .apiKey(llmApiKey)
                .modelName("gemini-1.5-flash") // Or your preferred Gemini responseformat
                .temperature(0.0) // Lower temperature for more deterministic routing
                .responseFormat(responseFormat) // Crucial step to enforce structured output
                .build();

        // 4. Build the AiService for the SupervisorAssistant
        return AiServices.builder(SupervisorAssistant.class)
                .chatModel(llm)
                // If the supervisor needs access to chat history to make decisions,
                // you would configure a ChatMemoryProvider here.
                .build();
    }



    @Override
    public Map<String, Object> apply(ChatState state) throws Exception {
        List<ChatMessage> history = state.messages();

        System.out.println("SupervisorAgent got input: " + history.getLast());

        // Get supervisor decision and instructions
        SupervisorOutput result = service.query(history);

        System.out.println("SupervisorAgent got output: " + result);

        // Create a message from supervisor's instructions
        AiMessage supervisorMessage = AiMessage.from(result.getValue());

        return Map.of(
                "next", result.getNext(),
                "messages", state.withMessage(supervisorMessage).messages()
        );
    }
}