package cz.cuni.mff.image_search_api;

import cz.cuni.mff.DatasetBaseProduct;

public record ProductMatch(
        double distance,
        double normalizedHammingDistance,
        DatasetBaseProduct product
) { }
