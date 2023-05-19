package org.example;

import org.example.image_match.ImagesUploader;
import org.example.image_search_api.ImageSearchApi;
import org.example.mongodb.ProductsUploader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import static org.example.image_search_api.SwaggerConfiguration.preInitializeSwagger;
import static org.example.image_search_api.SwaggerConfiguration.setupSwagger;

public class App
{
    private static final String DEFAULT_MONGO_DB_USERNAME = "fashion";
    private static final String DEFAULT_MONGO_DB_PASSWORD = "1234";

    private static final String DEFAULT_MYSQL_DB_USERNAME = "admin";
    private static final String DEFAULT_MYSQL_DB_PASSWORD = "admin";

    public static void main(String[] args) {
        System.out.println(
                "Program args (expecting arguments mongoUsername, mongoPassword, mysqlUsername and mysqlPassword" +
                        " in this order, otherwise using default credentials for both DBs): " + Arrays.toString(args));

        String mongoUsername = args.length > 0 ? args[0] : DEFAULT_MONGO_DB_USERNAME;
        String mongoPassword = args.length > 1 ? args[1] : DEFAULT_MONGO_DB_PASSWORD;

        System.out.printf("Mongo DB username: %s, password: %s%n", mongoUsername, mongoPassword);

        String mysqlUsername = args.length > 2 ? args[2] : DEFAULT_MYSQL_DB_USERNAME;
        String mysqlPassword = args.length > 3 ? args[3] : DEFAULT_MYSQL_DB_PASSWORD;
        System.out.printf("MySQL username: %s, password: %s%n", mysqlUsername, mysqlPassword);

        uploadProductsToMongo(mongoUsername, mongoPassword);
        uploadImagesToMySql(mysqlUsername, mysqlPassword);

        preInitializeSwagger();
        setupImageSearchApi(mysqlUsername, mysqlPassword, mongoUsername, mongoPassword);
        setupSwagger();
    }

    private static void uploadProductsToMongo(String mongoUsername, String mongoPassword) {
        try (var productsUploader = new ProductsUploader(mongoUsername, mongoPassword)) {
            productsUploader.uploadNewProducts();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void uploadImagesToMySql(String mysqlUsername, String mysqlPassword) {
        try (var imagesUploader = new ImagesUploader(mysqlUsername, mysqlPassword)) {
            imagesUploader.uploadProductImages();
        } catch (SQLException e) {
            throw new IllegalArgumentException(
                    "Invalid credentials for MySQL database provided, full message: " + e.getMessage()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setupImageSearchApi(
            String mysqlUsername,
            String mysqlPassword,
            String mongoUsername,
            String mongoPassword
    ) {
        try {
            var imageSearchApi = new ImageSearchApi(
                    mysqlUsername, mysqlPassword, mongoUsername, mongoPassword
            );

            imageSearchApi.setupUrlBasedSearchEndpoint();
        } catch (SQLException e) {
            throw new IllegalArgumentException(
                    "Invalid credentials for MySQL database provided, full message: " + e.getMessage()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
