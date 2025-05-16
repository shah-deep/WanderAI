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

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationSearchTool {

    private final TripAdvisorClient tripAdvisorClient;
    private final ObjectMapper objectMapper;

    @Value("${tripadvisor.api.key}")
    private String apiKey;

    @Tool("Searches for locations like hotels, attractions, or restaurants. Category is optional. Returns a list of found locations.")
    public List<SearchResult> searchLocation(
            @P("The search query") String searchQuery,
            @P("Optional: The category of the search, such as 'hotels', 'attractions', or 'restaurants'. If not provided or null, a general search is performed based on the query.") String category) {
        try {
            // Category can be null here, assuming tripAdvisorClient.searchLocations handles it
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
            log.error("Error searching for locations with query '{}' and category '{}'", searchQuery, category, e);
            return Collections.emptyList();
        }
    }

    private List<SearchResult> parseSearchResults(String jsonResponse) {
        List<SearchResult> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode data = root.get("data");

            if (data != null && data.isArray()) {
                for (JsonNode location : data) {
                    String locationId = location.path("location_id").asText();
                    String name = location.path("name").asText();
                    String address = location.path("address_obj").path("address_string").asText();

                    results.add(new SearchResult(locationId, name, address));
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing search results", e);
        }
        return results;
    }
}