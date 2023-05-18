package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ZalandoProduct (
        @BsonId()
        @BsonRepresentation(BsonType.STRING)
        String id,
        String url,
        String name,
        String sku,
        Brand brand,
        List<Flag> flags,
        boolean comingSoon,
        String thumbnail,
        List<String> images,
        List<String> videos,
        int reviewsCount,
        int ratingCount,
        float rating,
        RatingHistogram ratingHistogram,
        List<Review> reviews,
        Color color,
        String priceCurrency,
        Price price,
        List<Size> sizes,
        boolean available,
        String sizeAdvice,
        String navigationTargetGroup,
        Condition condition,
        List<AttributeCategory> attributeCategories
) implements MongoDocument {
    public record Brand(
            String name,
            String uri
    ) {}

    public record Flag(
            String formatted,
            String kind,
            String info
    ) {}

    public record RatingHistogram(
            int rating1Count,
            int rating3Count,
            int rating2Count,
            int rating4Count,
            int rating5Count
    ) {}

    public record Review(
            String authorName,
            String publishedAt,
            String text,
            int rating
    ) { }

    public record Color(
            String name,
            String label
    ) {}

    public record Price(
            BigDecimal original,
            BigDecimal current,
            BigDecimal promotional
    ) { }

    public record Size(
            String size,
            String sku,
            String stockStatus
    ) { }

    public record Attribute(
            String key,
            String value
    ) { }

    public record Condition(
            String kind
    ) { }

    public record AttributeCategory(
            String categoryId,
            String categoryName,
            List<Attribute> attributes
    ) { }
}