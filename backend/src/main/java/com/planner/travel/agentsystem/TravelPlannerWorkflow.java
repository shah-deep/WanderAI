package com.planner.travel.agentsystem;

import com.planner.travel.agentsystem.agents.DetailsAgent;
import com.planner.travel.agentsystem.agents.SearchAgent;
import com.planner.travel.agentsystem.agents.SupervisorAgent;
import com.planner.travel.agentsystem.state.ChatState;
import com.planner.travel.agentsystem.state.StateSerializer;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@RequiredArgsConstructor
@Component
public class TravelPlannerWorkflow {

    final SupervisorAgent supervisorAgent;
    final SearchAgent searchAgent;
    final DetailsAgent detailsAgent;

    public StateGraph<ChatState> createStateGraph() throws GraphStateException {
        // Define the StateGraph with ChatState and its serializer
        StateGraph<ChatState> workflow;
        workflow = new StateGraph<>(ChatState.SCHEMA, new StateSerializer())
            .addNode("supervisor", node_async(supervisorAgent))
            .addNode("search_agent", node_async(searchAgent))
            .addNode("details_agent", node_async(detailsAgent))

            // Set the entry point to the supervisor agent
            .addEdge(START, "supervisor")

            // Add conditional edges from the supervisor based on its output
            .addConditionalEdges(
                "supervisor",
                // The condition function extracts the "next" route from the ChatState
                // The SupervisorAgent is expected to put the next step's name in the state's "next" field.
                edge_async(state -> state.next().orElseThrow(() -> new IllegalStateException("Next step not found in state"))),

                    // Define the mapping from the "next" value to the actual node or END
                Map.of(
                    "Search Agent", "search_agent",
                    "Details Agent", "details_agent",
                    "User", END  // If "next" is "User", it means the supervisor will respond, so we end the flow.
                )
            )

            // After the SearchAgent or DetailsAgent finishes, route back to the supervisor
            .addEdge("search_agent", "supervisor")
            .addEdge("details_agent", "supervisor");

        return workflow;
    }
}