package cz.cuni.mff.dataset_products;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for deserializing JSON files with products from the Zoot and Zalando datasets.
 */
public class DatasetDeserializer {
    private final static Path ZOOT_PRODUCTS_DIR = Path.of("src/main/resources/datasets/zoot");
    private final static Path ZALANDO_PRODUCTS_DIR = Path.of("src/main/resources/datasets/zalando");
    private final static String INVALID_ZOOT_PRODUCTS_DIR_MESSAGE = "Dataset directories are not setup correctly." +
            "Package 'org.example' has to include 'datasets' directory" +
            "with 'zoot' subdirectory containing JSON files with Zoot products.";

    private final static String INVALID_ZALANDO_PRODUCTS_DIR_MESSAGE = "Dataset directories are not setup correctly." +
            "Package 'org.example' has to include 'datasets' directory" +
            "with 'zalando' subdirectory containing JSON files with Zalando products..";

    /**
     * Deserializes a JSON file with products from the Zoot or Zalando dataset.
     * The method uses the provided {@code objectType} to deserialize the JSON file
     * into a list of objects of the given type.
     *
     * @param jsonFile the JSON file with products
     * @param objectType the type of the object to deserialize
     * @param <T> the type of the object to deserialize
     * @return the list of deserialized objects (products)
     * @throws IOException if the file could not be read or the object could not be deserialized
     */
    public static <T> List<T> deserializeJsonCollection(File jsonFile, Class<T> objectType) throws IOException {
        System.out.println("Deserializing collection from: " + jsonFile.getPath());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var collectionType = mapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, objectType);

        return mapper.readValue(jsonFile, collectionType);
    }

    /**
     * Retrieves the list of files with products from the Zoot dataset.
     *
     * @return the list of files with collections of products from the Zoot dataset
     */
    public static List<File> getZootDatasetFiles () {
        try {
            return getProductFiles(ZOOT_PRODUCTS_DIR);
        } catch (Exception ex) {
            throw new RuntimeException(INVALID_ZOOT_PRODUCTS_DIR_MESSAGE + ", full message: " + ex.getMessage());
        }
    }

    /**
     * Retrieves the list of files with products from the Zalando dataset.
     *
     * @return the list of files with collections of products from the Zalando dataset
     */
    public static List<File> getZalandoDatasetFiles () {
        try {
            return getProductFiles(ZALANDO_PRODUCTS_DIR);
        } catch (Exception ex) {
            throw new RuntimeException(INVALID_ZALANDO_PRODUCTS_DIR_MESSAGE + ", full message: " + ex.getMessage());
        }
    }

    /**
     * Retrieves the list of files with products from the given directory.
     *
     * @param productsDirPath the path to the directory with product files
     * @return the list of files with collections of products from the given directory
     */
    private static List<File> getProductFiles (Path productsDirPath) {
        System.out.println("Reading products from dir with URL: " + productsDirPath);

        File productsDir = productsDirPath.toFile();

        System.out.println("Is '" + productsDir.getName() + "' a directory: " + productsDir.isDirectory());

        File[] productFiles = productsDir.listFiles();

        return productFiles != null
                ? Arrays.stream(productFiles).collect(Collectors.toList())
                : new ArrayList<>();
    }
}
