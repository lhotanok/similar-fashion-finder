package org.example.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class ProductsDbManager implements AutoCloseable {
    protected final MongoClient client;
    protected final MongoDatabase productsDb;
    protected final String connectionUri;
    protected static final String DB_NAME = "products";
    protected static final String ZOOT_PRODUCTS_COLLECTION = "zoot-products";
    protected static final String ZALANDO_PRODUCTS_COLLECTION = "zalando-products";

    public ProductsDbManager(String dbUsername, String dbPassword) {
        connectionUri = buildConnectionString(dbUsername, dbPassword);

        System.out.println("Creating client with Mongo connection URI: " + connectionUri);
        client = MongoClients.create(connectionUri);

        System.out.println("Client created, getting database: " + DB_NAME);
        productsDb = client.getDatabase(DB_NAME);
        System.out.println("Loaded database: " + DB_NAME);
    }

    @Override
    public void close() {
        System.out.println("Closing connection to MongoDB with products");
        client.close();
    }

    private String buildConnectionString(String dbUsername, String dbPassword) {
        String hostname = getHostname();

        return String.format(
                "mongodb://%s:%s@%s:27017/%s?authSource=admin",
                dbUsername,
                dbPassword,
                hostname,
                DB_NAME
        );
    }

    private String getHostname() {
        String mongoHost = System.getenv("MONGO_HOST");

        return mongoHost != null && !mongoHost.isEmpty()
                ? mongoHost
                : "localhost";
    }
}
