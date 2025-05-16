package com.planner.travel.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tripadvisor", url = "${tripadvisor.api.url}")
public interface TripAdvisorClient {

    @GetMapping("/location/{locationId}/attractions")
    ResponseEntity<String> getAttractions(
            @PathVariable String locationId,
            @RequestParam("key") String apiKey,
            @RequestParam("language") String language
    );

    @GetMapping("/location/search")
    ResponseEntity<String> searchLocations(
            @RequestParam("key") String apiKey,
            @RequestParam("searchQuery") String searchQuery,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam("language") String language
    );

    @GetMapping("/location/{locationId}/details")
    ResponseEntity<String> getLocationDetails(
            @PathVariable String locationId,
            @RequestParam("key") String apiKey,
            @RequestParam("language") String language,
            @RequestParam("currency") String currency
    );
}