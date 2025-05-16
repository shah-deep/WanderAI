package com.planner.travel.agentsystem.responseformat;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;
// It's good practice to add JsonPropertyDescription for clarity if the schema is complex,
// though for simple fields like these, it might be optional depending on the JsonSchema generator.

@Data
@NoArgsConstructor
public class SupervisorOutput {

    @JsonPropertyDescription("The next agent to route to, or 'User' if responding directly. Can be 'Search Agent', 'Details Agent', or 'User'.")
    private String next;

    @JsonPropertyDescription("The query/prompt for the next agent, or the direct response for the user.")
    private String value;

    @Override
    public String toString() {
        return "SupervisorOutput{" +
               "next='" + next + '\'' +
               ", value='" + value + '\'' +
               '}';
    }

}