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
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupervisorAgent implements NodeAction<ChatState> {

    @Value("${llm.api-key.gemini}")
    private String llmApiKey;

    @Value( "${llm.model-name.gemini}")
    private String llmModel;

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
                .modelName(llmModel) // Or your preferred Gemini response format
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
        try {
            List<ChatMessage> history = state.messages();
//            System.out.println("Supervisor Chat History: " + history);

            System.out.println("SupervisorAgent got input: " +
                (history.getLast() != null ? history.getLast() : "null message"));

            // Get supervisor decision and instructions
            SupervisorOutput result = service.query(history);

            if (result == null) {
                throw new RuntimeException("Failed to get response from supervisor");
            }

            System.out.println("SupervisorAgent got output: " + result);

            // Create a message from supervisor's instructions
            String supervisorValue = result.getValue();
            if (supervisorValue == null) {
                supervisorValue = "I couldn't process that request. Could you please try again?";
            }

            AiMessage supervisorMessage = AiMessage.from(supervisorValue);

            String next = result.getNext() != null ? result.getNext() : "User";

            return Map.of(
                    "next", next,
                    "messages", supervisorMessage
            );
        } catch (Exception e) {
            log.error("Error in SupervisorAgent: {}", e.getMessage(), e);
            // Return a fallback response instead of allowing exception to propagate
            AiMessage errorMessage = AiMessage.from(
                "I'm having trouble processing your request right now. Could you try again?"
            );
            return Map.of(
                    "next", "User",
                    "messages", errorMessage
            );
        }
    }
}