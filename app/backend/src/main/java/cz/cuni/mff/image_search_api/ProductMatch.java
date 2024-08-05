package cz.cuni.mff.image_search_api;

import cz.cuni.mff.dataset_products.DatasetBaseProduct;

/**
 * Represents a product match found by the image matching algorithm.
 * Each match contains the distance between the query image and the product image,
 * the normalized Hamming distance and the product itself.
 */
public record ProductMatch(
        double distance,
        double normalizedHammingDistance,
        DatasetBaseProduct product
) { }
