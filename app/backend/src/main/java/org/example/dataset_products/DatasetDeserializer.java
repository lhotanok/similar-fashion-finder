package org.example.dataset_products;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DatasetDeserializer {
    private final static Path ZOOT_PRODUCTS_DIR = Path.of("src/main/resources/datasets/zoot");
    private final static Path ZALANDO_PRODUCTS_DIR = Path.of("src/main/resources/datasets/zalando");
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
        try {
            return getProductFiles(ZOOT_PRODUCTS_DIR);
        } catch (Exception ex) {
            throw new RuntimeException(INVALID_ZOOT_PRODUCTS_DIR_MESSAGE + ", full message: " + ex.getMessage());
        }
    }

    public static List<File> getZalandoDatasetFiles () {
        try {
            return getProductFiles(ZALANDO_PRODUCTS_DIR);
        } catch (Exception ex) {
            throw new RuntimeException(INVALID_ZALANDO_PRODUCTS_DIR_MESSAGE + ", full message: " + ex.getMessage());
        }
    }

    private static List<File> getProductFiles (Path productsDirPath) throws URISyntaxException {
        System.out.println("Reading products from dir with URL: " + productsDirPath);

        File productsDir = productsDirPath.toFile();

        System.out.println("Is '" + productsDir.getName() + "' a directory: " + productsDir.isDirectory());

        File[] productFiles = productsDir.listFiles();

        return productFiles != null
                ? Arrays.stream(productFiles).collect(Collectors.toList())
                : new ArrayList<>();
    }
}
