package com.planner.travel.agentsystem.agents;

import com.planner.travel.client.TripAdvisorClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgentFactory {
    
    @Autowired
    private TripAdvisorClient tripAdvisorClient;
    
    @Value("${llm.api-key.gemini}")
    private String llmApiKey;
    
    @Value("${llm.model-name.gemini}")
    private String llmModel;
    
    @Value("${tripadvisor.api.key}")
    private String tripAdvisorApiKey;

    public SupervisorAgent createSupervisorAgent() {
        return new SupervisorAgent(llmApiKey, llmModel);
    }

    public SearchAgent createSearchAgent() {
        return new SearchAgent(llmApiKey, llmModel, tripAdvisorApiKey, tripAdvisorClient);
    }

    public DetailsAgent createDetailsAgent() {
        return new DetailsAgent(llmApiKey, llmModel, tripAdvisorApiKey, tripAdvisorClient);
    }
}