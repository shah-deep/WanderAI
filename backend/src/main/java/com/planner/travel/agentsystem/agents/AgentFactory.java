package com.planner.travel.agentsystem.agents;

import com.planner.travel.agentsystem.tools.LocationDetailsTool;
import com.planner.travel.agentsystem.tools.LocationSearchTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgentFactory {
    @Autowired
    private LocationSearchTool locationSearchTool;
    
    @Autowired
    private LocationDetailsTool locationDetailsTool;
    
    @Value("${llm.api-key.gemini}")
    private String llmApiKey;
    
    @Value("${llm.model-name.gemini}")
    private String llmModel;

    public SupervisorAgent createSupervisorAgent() {
        return new SupervisorAgent(llmApiKey, llmModel);
    }

    public SearchAgent createSearchAgent() {
        return new SearchAgent(locationSearchTool, llmApiKey, llmModel);
    }

    public DetailsAgent createDetailsAgent() {
        return new DetailsAgent(locationDetailsTool, llmApiKey, llmModel);
    }

}