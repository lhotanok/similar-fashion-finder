package org.example.image_search_api;

import org.example.DatasetBaseProduct;

public record ProductMatch(
        double distance,
        double normalizedHammingDistance,
        DatasetBaseProduct product
) { }
