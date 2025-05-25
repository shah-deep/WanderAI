package com.planner.travel.agentsystem.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planner.travel.agentsystem.state.SearchResult;
import com.planner.travel.client.TripAdvisorClient;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class LocationSearchTool {

    private final TripAdvisorClient tripAdvisorClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    @Tool("Searches for locations with no category is mentioned. Returns a list of found locations.")
    public List<SearchResult> searchLocationNoCategory(
            @P("The search query") String searchQuery) {
        return this.searchTripAdvisor(searchQuery, null);
    }

    @Tool("Searches for locations like hotels, attractions, restaurants or geos. Category is mentioned. Returns a list of found locations.")
    public List<SearchResult> searchLocationWithCategory(
            @P("The search query") String searchQuery,
            @P("The category of the search, such as 'hotels', 'attractions', 'restaurants', or 'geos'.") String category) {
        return this.searchTripAdvisor(searchQuery, category);
    }

    public List<SearchResult>searchTripAdvisor(String searchQuery, String category) {
        try {
            // Category can be null here, assuming tripAdvisorClient.searchLocations handles it
            if (category == null || category.isEmpty()) {
                category="Null";
            }
            // System.out.println("[Search Tool] searchLocation Tool got input: " + searchQuery + ", Category: " + category);
            ResponseEntity<String> response = tripAdvisorClient.searchLocations(
                    apiKey,
                    searchQuery,
                    category, // This might be null
                    "en"
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Failed to search locations. Status: {}", response.getStatusCode());
                return Collections.emptyList();
            }

            return parseSearchResults(response.getBody());
        } catch (Exception e) {
            log.error("Error searching for locations with query '{}' and category '{}': {}",
                searchQuery, category, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<SearchResult> parseSearchResults(String jsonResponse) {
        List<SearchResult> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode data = root.get("data");

            if (data != null && data.isArray() && !data.isEmpty()) {
                for (JsonNode location : data) {
                    String locationId = location.path("location_id").asText();
                    String name = location.path("name").asText();
                    String address = location.path("address_obj").path("address_string").asText();

                    results.add(new SearchResult(locationId, name, address));
                }
            } else {
                log.info("No results found in Search API response or empty data array");
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing search results: ", e);
        }
        return results;
    }
}