package cz.cuni.mff.dataset_products;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

/**
 * Represents a product extracted from Zoot website.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ZootProduct (
        @BsonId()
        @BsonRepresentation(BsonType.STRING)
        String id,
        String url,
        String name,
        String priceCurrency,
        Price currentBestPrice,
        Price originalPrice,
        String saleCode,
        String thumbnail,
        List<String> images,
        Brand brand,
        List<Breadcrumb> breadcrumbs,
        String description,
        List<Attribute> attributes,
        List<Size> sizes,
        boolean available
) implements DatasetBaseProduct {
    public record Price(
            BigDecimal value,
            String formattedPrice
    ) { }

    public record Brand(
            String link,
            String logo
    ) { }

    public record Breadcrumb(
            String text,
            String url
    ) { }

    public record Attribute(
            String key,
            String value
    ) { }

    public record Size(
            String size,
            boolean available
    ) { }
}