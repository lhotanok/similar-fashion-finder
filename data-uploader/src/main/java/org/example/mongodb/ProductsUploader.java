package org.example.mongodb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoSecurityException;
import com.mongodb.client.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ProductsUploader {
    private final MongoClient client;
    private final MongoDatabase productsDb;
    private final String connectionUri;
    private static final String DB_NAME = "products";
    private static final String ZOOT_PRODUCTS_COLLECTION = "zoot-products";
    private static final String ZALANDO_PRODUCTS_COLLECTION = "zalando-products";
    private final static URL ZOOT_PRODUCTS_DIR = ProductsUploader.class.getResource("/datasets/zoot");
    private final static URL ZALANDO_PRODUCTS_DIR = ProductsUploader.class.getResource("/datasets/zalando");
    private final static String INVALID_PRODUCTS_DIR_MESSAGE = "Dataset directories are not setup correctly." +
            "Package 'org.example' has to include 'datasets' directory" +
            "with 'zalando' and 'zoot' subdirectories.";

    public ProductsUploader(String dbUsername, String dbPassword) {
        connectionUri = String.format(
                "mongodb://%s:%s@localhost:27017/%s?authSource=admin",
                dbUsername,
                dbPassword,
                DB_NAME
        );

        System.out.println("Creating client with Mongo connection URI: " + connectionUri);
        client = MongoClients.create(connectionUri);

        System.out.println("Client created, getting database: " + DB_NAME);
        productsDb = client.getDatabase(DB_NAME);
        System.out.println("Loaded database: " + DB_NAME);
    }

    public void uploadNewProducts() throws IOException, IllegalArgumentException {
        List<File> zootFiles;
        List<File> zalandoFiles;

        if (ZOOT_PRODUCTS_DIR == null || ZALANDO_PRODUCTS_DIR == null) {
            throw new RuntimeException(INVALID_PRODUCTS_DIR_MESSAGE);
        }

        try {
            zootFiles = getProductFiles(ZOOT_PRODUCTS_DIR);
            zalandoFiles = getProductFiles(ZALANDO_PRODUCTS_DIR);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(INVALID_PRODUCTS_DIR_MESSAGE);
        }

        try {
            for (File file: zootFiles) {
                tryUploadProducts(
                        file, ZOOT_PRODUCTS_COLLECTION, ZootProduct.class
                );
            }

            for (File file: zalandoFiles) {
                tryUploadProducts(
                        file, ZALANDO_PRODUCTS_COLLECTION, ZalandoProduct.class
                );
            }
        } catch (MongoSecurityException ex) {
            throw new IllegalArgumentException(
                    "Could not connect to MongoDB. Invalid connection string: " + connectionUri
            );
        }
    }

    private <ProductType extends MongoDocument> void uploadProductsFromFile(
            File productsFile,
            String collectionName,
            Class<ProductType> productType
    ) throws IOException, MongoSecurityException {
        if (!collectionExists(collectionName)) {
            System.out.println("Creating collection: " + collectionName);
            productsDb.createCollection(collectionName);
        }

        System.out.println("Getting collection: " + collectionName);
        MongoCollection<ProductType> collection = productsDb.getCollection(collectionName, productType);

        List<ProductType> products = deserializeJsonCollection(productsFile, productType);
        System.out.println("Loaded products: " + products.size());

        List<ProductType> productsToUpload = filterNewProducts(products, collection);
        System.out.println(productsToUpload.size() + " products will be uploaded");

        if (productsToUpload.size() == 0) {
            return;
        }

        try {
            var result = collection.insertMany(productsToUpload);

            String resultMessage = String.format(
                    "Inserted %d products into collection %s",
                    result.getInsertedIds().size(),
                    collectionName
            );

            System.out.println(resultMessage);
        } catch (MongoBulkWriteException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private <ProductType extends MongoDocument> List<ProductType> filterNewProducts(
            List<ProductType> productsToUpload, MongoCollection<ProductType> collection
    ) {
        Map<String, Boolean> uploadedIds = new HashMap<>();
        collection.distinct("_id", String.class).forEach(id -> uploadedIds.put(id, true));

        var newProducts = productsToUpload.stream()
                .filter(product -> !uploadedIds.containsKey(product.id()));

        return newProducts.collect(Collectors.toList());
    }

    private boolean collectionExists (String collectionName) throws MongoSecurityException {
        System.out.println("Checking if collection already exists: " + collectionName);

        for (String existingName: productsDb.listCollectionNames()) {
            System.out.println("Collection: " + existingName);

            if (existingName.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }

        return false;
    }
    private <T extends  MongoDocument> List<T> deserializeJsonCollection(File jsonFile, Class<T> objectType)
            throws IOException {
        System.out.println("Deserializing collection from: " + jsonFile.getPath());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var collectionType = mapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, objectType);

        return mapper.readValue(jsonFile, collectionType);
    }

    private static List<File> getProductFiles (URL productsDirUrl) throws URISyntaxException {
        File productsDir = new File(productsDirUrl.toURI());

        System.out.println("Is '" + productsDir.getName() + "' a directory: " + productsDir.isDirectory());

        File[] productFiles = productsDir.listFiles();

        return productFiles != null
                ? Arrays.stream(productFiles).collect(Collectors.toList())
                : new ArrayList<>();
    }

    private <ProductType extends MongoDocument> void tryUploadProducts (
            File productsFile,
            String collectionName,
            Class<ProductType> productType
    ) throws IOException, MongoSecurityException {
        try {
            uploadProductsFromFile(
                    productsFile,
                    collectionName,
                    productType
            );
        } catch (IOException ex) {
            System.out.println("Products could not be deserialized.");
            throw ex;
        }
    }
}
