package org.example;

import java.io.IOException;
import java.util.Arrays;

public class App
{
    private static final String DEFAULT_MONGO_DB_USERNAME = "fashion";
    private static final String DEFAULT_MONGO_DB_PASSWORD = "1234";
    private static final String ZOOT_PRODUCTS_COLLECTION = "zoot-products";

    public static void main( String[] args ) {
        System.out.println("Args: " + args);
        System.out.println( "Uploading products to MongoDB" );

        String mongoDbUsername = args.length > 0 ? args[0] : DEFAULT_MONGO_DB_USERNAME;
        String mongoDbPassword = args.length > 1 ? args[1] : DEFAULT_MONGO_DB_PASSWORD;
        String d

        System.out.println("Mongo DB username: " + mongoDbUsername);
        System.out.println("Mongo DB password: " + mongoDbPassword);

        ProductsUploader uploader = new ProductsUploader(mongoDbUsername, mongoDbPassword);

        try {
            uploader.uploadProducts(
                    "D:\\OneDrive - Univerzita Karlova\\Dokumenty\\Skola\\MFF_UK\\Rocnik_4\\Java\\Zapoctovy_program\\similar-fashion-finder\\data-uploader\\datasets\\data\\zoot-women-fashion.json",
                    ZOOT_PRODUCTS_COLLECTION,
                    ZootProduct.class
            );
        } catch (IOException ex) {
            System.out.println("Products could not be deserialized.");
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }
    }
}
