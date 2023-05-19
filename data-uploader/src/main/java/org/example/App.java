package org.example;

import org.example.mongodb.ProductsUploader;

import java.io.IOException;
import java.util.Arrays;

public class App
{
    private static final String DEFAULT_MONGO_DB_USERNAME = "fashion";
    private static final String DEFAULT_MONGO_DB_PASSWORD = "1234";

    public static void main( String[] args ) {
        System.out.println("Program args: " + Arrays.toString(args));

        String mongoDbUsername = args.length > 0 ? args[0] : DEFAULT_MONGO_DB_USERNAME;
        String mongoDbPassword = args.length > 1 ? args[1] : DEFAULT_MONGO_DB_PASSWORD;

        System.out.printf("Mongo DB username: %s, password: %s%n", mongoDbUsername, mongoDbPassword);

        ProductsUploader uploader = new ProductsUploader(mongoDbUsername, mongoDbPassword);

        try {
            uploader.uploadNewProducts();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
