package com.planner.travel.agentsystem.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planner.travel.agentsystem.state.LocationDetails;
import com.planner.travel.client.TripAdvisorClient;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool; // Added import for @Tool
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationDetailsTool {

    private final TripAdvisorClient tripAdvisorClient;
    private final ObjectMapper objectMapper;

    @Value("${tripadvisor.api.key}")
    private String apiKey;

    @Tool("Gets detailed information for a specific location using its unique locationId. Returns comprehensive details about the location.")
    public LocationDetails getLocationDetails(
            @P("The unique identifier for a location on Tripadvisor, obtained from a location search") String locationId) {
        try {
            ResponseEntity<String> response = tripAdvisorClient.getLocationDetails(
                    locationId,
                    apiKey,
                    "en",
                    "USD"
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Failed to get location details. Status: {}", response.getStatusCode());
                return null;
            }

            return parseLocationDetails(response.getBody());
        } catch (Exception e) {
            log.error("Error getting location details for locationId {}: {}", locationId, e.getMessage(), e);
            return null;
        }
    }

    private LocationDetails parseLocationDetails(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            String locationIdFromJson = root.path("location_id").asText();
            String name = root.path("name").asText();
            String description = root.path("description").asText("");
            String address = extractAddress(root); // Uses address_obj.address_string
            double rating = root.path("rating").asDouble(0.0);
            int numReviews = root.path("num_reviews").asInt(0);
            String webUrl = root.path("web_url").asText("");
            String website = root.path("website").asText("");
            String latitude = root.path("latitude").asText();
            String longitude = root.path("longitude").asText();
            String timezone = root.path("timezone").asText();
            String phone = root.path("phone").asText();
            String writeReviewUrl = root.path("write_review").asText();
            String rankingString = root.path("ranking_data").path("ranking_string").asText();
            String ratingImageUrl = root.path("rating_image_url").asText();
            Map<String, String> reviewRatingCounts = extractReviewRatingCounts(root.path("review_rating_count"));
            int photoCount = root.path("photo_count").asInt(0);
            String seeAllPhotosUrl = root.path("see_all_photos").asText();
            List<String> weekdayText = extractWeekdayText(root.path("hours"));
            String categoryName = root.path("category").path("name").asText();
            String categoryLocalizedName = root.path("category").path("localized_name").asText();
            List<LocationDetails.SubcategoryInfo> subcategories = extractSubcategories(root.path("subcategory"));
            List<LocationDetails.GroupInfo> groups = extractGroups(root.path("groups"));
            List<LocationDetails.TripTypeInfo> tripTypes = extractTripTypes(root.path("trip_types"));
            List<LocationDetails.AwardInfo> awards = extractAwards(root.path("awards"));


            return new LocationDetails(
                    locationIdFromJson, name, description, address, rating,
                    numReviews, webUrl, website, latitude, longitude,
                    timezone, phone, writeReviewUrl, rankingString, ratingImageUrl,
                    reviewRatingCounts, photoCount, seeAllPhotosUrl, weekdayText,
                    categoryName, categoryLocalizedName, subcategories, groups,
                    tripTypes, awards
            );
        } catch (JsonProcessingException e) {
            log.error("Error parsing location details JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    private String extractAddress(JsonNode rootNode) {
        JsonNode addressObjNode = rootNode.path("address_obj");
        if (!addressObjNode.isMissingNode() && !addressObjNode.path("address_string").asText("").isEmpty()) {
            return addressObjNode.path("address_string").asText();
        }
       return ""; // Return empty if not found, or handle as per requirements.
    }

    private Map<String, String> extractReviewRatingCounts(JsonNode reviewRatingNode) {
        Map<String, String> counts = new HashMap<>();
        if (reviewRatingNode.isObject()) {
            reviewRatingNode.fields().forEachRemaining(entry -> counts.put(entry.getKey(), entry.getValue().asText()));
        }
        return counts;
    }

    private List<String> extractWeekdayText(JsonNode hoursNode) {
        List<String> weekdayTextList = new ArrayList<>();
        JsonNode weekdayTextNode = hoursNode.path("weekday_text");
        if (weekdayTextNode.isArray()) {
            for (JsonNode textNode : weekdayTextNode) {
                weekdayTextList.add(textNode.asText());
            }
        }
        return weekdayTextList;
    }

    private List<LocationDetails.SubcategoryInfo> extractSubcategories(JsonNode subcategoriesNode) {
        List<LocationDetails.SubcategoryInfo> subcategoryList = new ArrayList<>();
        if (subcategoriesNode.isArray()) {
            for (JsonNode subcategoryNode : subcategoriesNode) {
                String name = subcategoryNode.path("name").asText();
                String localizedName = subcategoryNode.path("localized_name").asText();
                subcategoryList.add(new LocationDetails.SubcategoryInfo(name, localizedName));
            }
        }
        return subcategoryList;
    }

    private List<LocationDetails.GroupInfo> extractGroups(JsonNode groupsNode) {
        List<LocationDetails.GroupInfo> groupList = new ArrayList<>();
        if (groupsNode.isArray()) {
            for (JsonNode groupNode : groupsNode) {
                String name = groupNode.path("name").asText();
                String localizedName = groupNode.path("localized_name").asText();
                List<LocationDetails.CategoryInfo> categories = new ArrayList<>();
                JsonNode categoriesNode = groupNode.path("categories");
                if (categoriesNode.isArray()) {
                    for (JsonNode categoryNode : categoriesNode) {
                        String catName = categoryNode.path("name").asText();
                        String catLocalizedName = categoryNode.path("localized_name").asText();
                        categories.add(new LocationDetails.CategoryInfo(catName, catLocalizedName));
                    }
                }
                groupList.add(new LocationDetails.GroupInfo(name, localizedName, categories));
            }
        }
        return groupList;
    }

    private List<LocationDetails.TripTypeInfo> extractTripTypes(JsonNode tripTypesNode) {
        List<LocationDetails.TripTypeInfo> tripTypeList = new ArrayList<>();
        if (tripTypesNode.isArray()) {
            for (JsonNode tripTypeNode : tripTypesNode) {
                String name = tripTypeNode.path("name").asText();
                String localizedName = tripTypeNode.path("localized_name").asText();
                String value = tripTypeNode.path("value").asText();
                tripTypeList.add(new LocationDetails.TripTypeInfo(name, localizedName, value));
            }
        }
        return tripTypeList;
    }

    private List<LocationDetails.AwardInfo> extractAwards(JsonNode awardsNode) {
        List<LocationDetails.AwardInfo> awardList = new ArrayList<>();
        if (awardsNode.isArray()) {
            for (JsonNode awardNode : awardsNode) {
                String awardType = awardNode.path("award_type").asText();
                String year = awardNode.path("year").asText();
                String displayName = awardNode.path("display_name").asText();
                String imageUrl = awardNode.path("images").path("large").asText();
                if (imageUrl.isEmpty()) {
                    imageUrl = awardNode.path("images").path("small").asText();
                }
                 if (imageUrl.isEmpty()) {
                    imageUrl = awardNode.path("images").path("tiny").asText();
                }
                List<String> categories = new ArrayList<>();
                JsonNode categoriesNode = awardNode.path("categories");
                if (categoriesNode.isArray()) {
                    for (JsonNode categoryNode : categoriesNode) {
                        categories.add(categoryNode.asText());
                    }
                }
                awardList.add(new LocationDetails.AwardInfo(awardType, year, displayName, imageUrl, categories));
            }
        }
        return awardList;
    }
}