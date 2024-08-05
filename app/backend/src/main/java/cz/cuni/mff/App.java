package cz.cuni.mff;

import cz.cuni.mff.image_match.ImagesUploader;
import cz.cuni.mff.image_search_api.CorsFilter;
import cz.cuni.mff.image_search_api.ImageSearchApi;
import cz.cuni.mff.image_search_api.SwaggerConfiguration;
import cz.cuni.mff.mongodb.ProductsUploader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Main class of the Similar Fashion Finder application, responsible for orchestrating the whole process.
 */
public class App
{
    private static final String DEFAULT_MONGO_DB_USERNAME = "fashion";
    private static final String DEFAULT_MONGO_DB_PASSWORD = "1234";

    private static final String DEFAULT_H2_DB_USERNAME = "admin";
    private static final String DEFAULT_H2_DB_PASSWORD = "admin";

    public static void main(String[] args) {
        System.out.println(
                "Program args (expecting arguments mongoUsername, mongoPassword, h2Username and h2Password" +
                        " in this order, otherwise using default credentials for both DBs): " + Arrays.toString(args));

        String mongoUsername = args.length > 0 ? args[0] : DEFAULT_MONGO_DB_USERNAME;
        String mongoPassword = args.length > 1 ? args[1] : DEFAULT_MONGO_DB_PASSWORD;

        System.out.printf("Mongo DB username: %s, password: %s%n", mongoUsername, mongoPassword);

        String h2Username = args.length > 2 ? args[2] : DEFAULT_H2_DB_USERNAME;
        String h2Password = args.length > 3 ? args[3] : DEFAULT_H2_DB_PASSWORD;
        System.out.printf("H2 DB username: %s, password: %s%n", h2Username, h2Password);

        SwaggerConfiguration.setupSwagger();

        uploadProductsToMongo(mongoUsername, mongoPassword);
        uploadImagesToH2Db(h2Username, h2Password);

        setupImageSearchApi(h2Username, h2Password, mongoUsername, mongoPassword);
    }

    /**
     * Uploads new product items from JSON resource files to MongoDB collections.
     * Each product item is represented by a separate MongoDB document.
     *
     * @param mongoUsername username for MongoDB
     * @param mongoPassword password for MongoDB
     */
    private static void uploadProductsToMongo(String mongoUsername, String mongoPassword) {
        try (var productsUploader = new ProductsUploader(mongoUsername, mongoPassword)) {
            productsUploader.uploadNewProducts();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Uploads main product images to H2 database.
     * The images are downloaded using the URLs provided in the product JSON files.
     * Uploaded images are to be used for image-based product search.
     *
     * @param mysqlUsername username for H2 database
     * @param mysqlPassword password for H2 database
     */
    private static void uploadImagesToH2Db(String mysqlUsername, String mysqlPassword) {
        try (var imagesUploader = new ImagesUploader(mysqlUsername, mysqlPassword)) {
            imagesUploader.uploadProductImages();
        } catch (SQLException e) {
            throw new IllegalArgumentException(
                    "Invalid credentials for H2 database provided, full message: " + e.getMessage()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up the image search API, which provides endpoints for image-based product search.
     * The API is built on top of the Spark framework.
     *
     * @param h2Username username for H2 database
     * @param h2Password password for H2 database
     * @param mongoUsername username for MongoDB
     * @param mongoPassword password for MongoDB
     */
    private static void setupImageSearchApi(
            String h2Username,
            String h2Password,
            String mongoUsername,
            String mongoPassword
    ) {
        CorsFilter.apply();

        try {
            var imageSearchApi = new ImageSearchApi(
                    h2Username, h2Password, mongoUsername, mongoPassword
            );

            imageSearchApi.setupUrlBasedSearchEndpoint();
        } catch (SQLException e) {
            throw new IllegalArgumentException(
                    "Invalid credentials for H2 database provided, full message: " + e.getMessage()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
