package org.example.dataset_products;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DatasetDeserializer {
    private final static URL ZOOT_PRODUCTS_DIR = DatasetDeserializer.class.getResource("/datasets/zoot");
    private final static URL ZALANDO_PRODUCTS_DIR = DatasetDeserializer.class.getResource("/datasets/zalando");
    private final static String INVALID_ZOOT_PRODUCTS_DIR_MESSAGE = "Dataset directories are not setup correctly." +
            "Package 'org.example' has to include 'datasets' directory" +
            "with 'zoot' subdirectory containing JSON files with Zoot products.";

    private final static String INVALID_ZALANDO_PRODUCTS_DIR_MESSAGE = "Dataset directories are not setup correctly." +
            "Package 'org.example' has to include 'datasets' directory" +
            "with 'zalando' subdirectory containing JSON files with Zalando products..";
    public static <T> List<T> deserializeJsonCollection(File jsonFile, Class<T> objectType) throws IOException {
        System.out.println("Deserializing collection from: " + jsonFile.getPath());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var collectionType = mapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, objectType);

        return mapper.readValue(jsonFile, collectionType);
    }

    public static List<File> getZootDatasetFiles () {
        if (ZOOT_PRODUCTS_DIR == null || ZALANDO_PRODUCTS_DIR == null) {
            throw new RuntimeException(INVALID_ZOOT_PRODUCTS_DIR_MESSAGE);
        }

        try {
            return getProductFiles(ZOOT_PRODUCTS_DIR);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(INVALID_ZOOT_PRODUCTS_DIR_MESSAGE);
        }
    }

    public static List<File> getZalandoDatasetFiles () {
        if (ZOOT_PRODUCTS_DIR == null || ZALANDO_PRODUCTS_DIR == null) {
            throw new RuntimeException(INVALID_ZALANDO_PRODUCTS_DIR_MESSAGE);
        }

        try {
            return getProductFiles(ZALANDO_PRODUCTS_DIR);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(INVALID_ZALANDO_PRODUCTS_DIR_MESSAGE);
        }
    }

    private static List<File> getProductFiles (URL productsDirUrl) throws URISyntaxException {
        File productsDir = new File(productsDirUrl.toURI());

        System.out.println("Is '" + productsDir.getName() + "' a directory: " + productsDir.isDirectory());

        File[] productFiles = productsDir.listFiles();

        return productFiles != null
                ? Arrays.stream(productFiles).collect(Collectors.toList())
                : new ArrayList<>();
    }
}
