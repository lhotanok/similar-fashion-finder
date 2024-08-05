package cz.cuni.mff.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Manages the connection to the MongoDB database with products.
 * It initializes the connection and provides access to the database
 * through the {@link #productsDb} field.
 * {@link ProductsDbManager} is expected to be used as a base class for classes that need to
 * interact with the products' database.
 */
public class ProductsDbManager implements AutoCloseable {
    protected final MongoClient client;
    protected final MongoDatabase productsDb;
    protected final String connectionUri;
    protected static final String DB_NAME = "products";
    protected static final String ZOOT_PRODUCTS_COLLECTION = "zoot-products";
    protected static final String ZALANDO_PRODUCTS_COLLECTION = "zalando-products";

    /**
     * Initializes the connection to the MongoDB database with products.
     * The connection is established using the provided username and password.
     * A default database name "products" is used.
     *
     * @param dbUsername the username for the MongoDB database
     * @param dbPassword the password for the MongoDB database
     */
    public ProductsDbManager(String dbUsername, String dbPassword) {
        connectionUri = buildConnectionString(dbUsername, dbPassword);

        System.out.println("Creating client with Mongo connection URI: " + connectionUri);
        client = MongoClients.create(connectionUri);

        System.out.println("Client created, getting database: " + DB_NAME);
        productsDb = client.getDatabase(DB_NAME);
        System.out.println("Loaded database: " + DB_NAME);
    }

    /**
     * Closes the connection to the MongoDB database with products.
     */
    @Override
    public void close() {
        System.out.println("Closing connection to MongoDB with products");
        client.close();
    }

    /**
     * Builds the connection string for the MongoDB database.
     * The connection string is constructed from the provided username and password.
     *
     * @param dbUsername the username for the MongoDB database
     * @param dbPassword the password for the MongoDB database
     * @return the connection string for the MongoDB database
     */
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

    /**
     * Retrieves the hostname of the MongoDB server.
     * The hostname is read from the environment variable "MONGO_HOST".
     * If the variable is not set, the default hostname "localhost" is used.
     *
     * @return the hostname of the MongoDB server
     */
    private String getHostname() {
        String mongoHost = System.getenv("MONGO_HOST");

        return mongoHost != null && !mongoHost.isEmpty()
                ? mongoHost
                : "localhost";
    }
}
