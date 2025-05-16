package com.planner.travel.agentsystem.state;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SearchResult(
        @JsonProperty("location_id") String locationId,
        @JsonProperty("name") String name,
        @JsonProperty("address") String address
) {
    @Override
    public String toString() {
        return String.format("%s - %s", name, address);
    }
}