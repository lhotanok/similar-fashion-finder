package org.example.mongodb;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoSecurityException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.dataset_products.DatasetDeserializer;
import org.example.DatasetBaseProduct;
import org.example.ZalandoProduct;
import org.example.ZootProduct;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductsUploader implements AutoCloseable {
    private final MongoClient client;
    private final MongoDatabase productsDb;
    private final String connectionUri;
    private static final String DB_NAME = "products";
    private static final String ZOOT_PRODUCTS_COLLECTION = "zoot-products";
    private static final String ZALANDO_PRODUCTS_COLLECTION = "zalando-products";

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
        try {
            for (File zootFile: DatasetDeserializer.getZootDatasetFiles()) {
                tryUploadProductsToMongo(
                        zootFile, ZOOT_PRODUCTS_COLLECTION, ZootProduct.class
                );
            }

            for (File zalandoFile: DatasetDeserializer.getZalandoDatasetFiles()) {
                tryUploadProductsToMongo(
                        zalandoFile, ZALANDO_PRODUCTS_COLLECTION, ZalandoProduct.class
                );
            }
        } catch (MongoSecurityException ex) {
            throw new IllegalArgumentException(
                    "Could not connect to MongoDB. Invalid connection string: " + connectionUri
            );
        }
    }


    @Override
    public void close() {
        System.out.println("Closing connection to MongoDB with products");
        client.close();
    }

    private <ProductType extends DatasetBaseProduct> void uploadProductsFromFile(
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

        // In the end, thumbnail index should not be needed
        /*System.out.println(collectionName + ": Creating index on field 'thumbnail' for efficient retrieval " +
                "based on thumbnail");
        Document thumbnailIndex = new Document("thumbnail", 1);
        collection.createIndex(thumbnailIndex);*/

        List<ProductType> products = DatasetDeserializer.deserializeJsonCollection(productsFile, productType);
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

    private <ProductType extends DatasetBaseProduct> List<ProductType> filterNewProducts(
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
            if (existingName.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }

        return false;
    }

    private <ProductType extends DatasetBaseProduct> void tryUploadProductsToMongo(
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
