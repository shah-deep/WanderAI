package com.planner.travel.agentsystem.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record LocationDetails(
    @JsonProperty("location_id") String locationId,
    @JsonProperty("name") String name,
    @JsonProperty("description") String description,
    @JsonProperty("address") String address,
    @JsonProperty("rating") double rating,
    @JsonProperty("num_reviews") int numReviews,
    @JsonProperty("web_url") String webUrl,
    @JsonProperty("website") String website,
    @JsonProperty("latitude") String latitude,
    @JsonProperty("longitude") String longitude,
    @JsonProperty("timezone") String timezone,
    @JsonProperty("phone") String phone,
    @JsonProperty("write_review_url") String writeReviewUrl,
    @JsonProperty("ranking_string") String rankingString,
    @JsonProperty("rating_image_url") String ratingImageUrl,
    @JsonProperty("review_rating_counts") Map<String, String> reviewRatingCounts,
    @JsonProperty("photo_count") int photoCount,
    @JsonProperty("see_all_photos_url") String seeAllPhotosUrl,
    @JsonProperty("weekday_text") List<String> weekdayText,
    @JsonProperty("category_name") String categoryName,
    @JsonProperty("category_localized_name") String categoryLocalizedName,
    @JsonProperty("subcategories") List<SubcategoryInfo> subcategories,
    @JsonProperty("groups") List<GroupInfo> groups,
    @JsonProperty("trip_types") List<TripTypeInfo> tripTypes,
    @JsonProperty("awards") List<AwardInfo> awards
) {

    public record SubcategoryInfo(
        @JsonProperty("name") String name,
        @JsonProperty("localized_name") String localizedName
    ) {}

    public record CategoryInfo(
        @JsonProperty("name") String name,
        @JsonProperty("localized_name") String localizedName
    ) {}

    public record GroupInfo(
        @JsonProperty("name") String name,
        @JsonProperty("localized_name") String localizedName,
        @JsonProperty("categories") List<CategoryInfo> categories
    ) {}

    public record TripTypeInfo(
        @JsonProperty("name") String name,
        @JsonProperty("localized_name") String localizedName,
        @JsonProperty("value") String value
    ) {}

    public record AwardInfo(
        @JsonProperty("award_type") String awardType,
        @JsonProperty("year") String year,
        @JsonProperty("display_name") String displayName,
        @JsonProperty("image_url") String imageUrl, // Simplified from images.large.url
        @JsonProperty("categories") List<String> categories
    ) {}

    @Override
    public String toString() {
        return String.format("""
            Name: %s
            Address: %s
            Rating: %.1f (%d reviews)
            Description: %s
            Ranking: %s
            Category: %s
            Phone: %s
            Website: %s
            TripAdvisor URL: %s
            Hours: %s
            Awards: %s
            """,
                name,
                address,
                rating,
                numReviews,
                description != null && description.length() > 100 ? description.substring(0, 100) + "..." : description,
                rankingString,
                categoryLocalizedName,
                phone,
                website,
                webUrl,
                weekdayText != null ? String.join("; ", weekdayText) : "N/A",
                awards != null ? awards.stream().map(AwardInfo::displayName).collect(Collectors.joining(", ")) : "N/A"
        );
    }
}