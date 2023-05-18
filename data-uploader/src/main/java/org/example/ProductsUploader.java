package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ProductsUploader {
    private final MongoClient client;
    private final MongoDatabase productsDb;
    private static final String DB_NAME = "products";

    public ProductsUploader(String dbUsername, String dbPassword) {
        String connectionUri = String.format(
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

    public <ProductType extends MongoDocument> void uploadProducts(
            File productsFile,
            String collectionName,
            Class<ProductType> productType
    ) throws IOException {
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

    private boolean collectionExists (String collectionName) {
        System.out.println("Checking if collection already exists: " + collectionName);

        for (String existingName: productsDb.listCollectionNames()) {
            System.out.println("Collection: " + existingName);

            if (existingName.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }

        return false;
    }
    private <T> List<T> deserializeJsonCollection(File jsonFile, Class<T> objectType) throws IOException {
        System.out.println("Deserializing collection from: " + jsonFile.getPath());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var collectionType = mapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, objectType);

        return mapper.readValue(jsonFile, collectionType);
    }
}
