package cz.cuni.mff.dataset_products;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

/**
 * Interface for dataset products that are used in the image matching process.
 * Each product is represented by a single document in the MongoDB collection.
 */
public interface DatasetBaseProduct {
    /**
     * Returns the unique identifier of the product.
     * The identifier is used as the primary key in the MongoDB collection.
     */
    @BsonId()
    @BsonRepresentation(BsonType.STRING)
    String id();
    /**
     * Returns a URL of the main product image (thumbnail).
     */
    String thumbnail();
}
