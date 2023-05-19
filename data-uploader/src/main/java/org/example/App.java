package org.example;

import org.example.image_match.ImagesUploader;
import org.example.mongodb.ProductsUploader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

public class App
{
    private static final String DEFAULT_MONGO_DB_USERNAME = "fashion";
    private static final String DEFAULT_MONGO_DB_PASSWORD = "1234";

    private static final String DEFAULT_MYSQL_DB_USERNAME = "root";
    private static final String DEFAULT_MYSQL_DB_PASSWORD = "";

    public static void main(String[] args) {
        System.out.println(
                "Program args (expecting arguments mongoUsername, mongoPassword, mysqlUsername and mysqlPassword" +
                        " in this order, otherwise using default credentials for both DBs): " + Arrays.toString(args));

        String mongoDbUsername = args.length > 0 ? args[0] : DEFAULT_MONGO_DB_USERNAME;
        String mongoDbPassword = args.length > 1 ? args[1] : DEFAULT_MONGO_DB_PASSWORD;

        String mysqlUsername = args.length > 2 ? args[2] : DEFAULT_MYSQL_DB_USERNAME;
        String mysqlPassword = args.length > 3 ? args[3] : DEFAULT_MYSQL_DB_PASSWORD;

        System.out.printf("Mongo DB username: %s, password: %s%n", mongoDbUsername, mongoDbPassword);
        System.out.printf("MySQL username: %s, password: %s%n", mysqlUsername, mysqlPassword);

        try (var productsUploader = new ProductsUploader(mongoDbUsername, mongoDbPassword)) {
            productsUploader.uploadNewProducts();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
}
