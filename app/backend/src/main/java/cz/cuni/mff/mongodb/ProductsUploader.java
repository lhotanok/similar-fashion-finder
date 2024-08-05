package cz.cuni.mff.mongodb;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoSecurityException;
import com.mongodb.client.MongoCollection;
import cz.cuni.mff.dataset_products.DatasetBaseProduct;
import cz.cuni.mff.dataset_products.ZalandoProduct;
import cz.cuni.mff.dataset_products.ZootProduct;
import cz.cuni.mff.dataset_products.DatasetDeserializer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class for uploading products from the Zoot and Zalando datasets to the MongoDB collections.
 */
public class ProductsUploader extends ProductsDbManager {

    /**
     * Initializes the MongoDB connection with the provided credentials.
     *
     * @param dbUsername MongoDB username
     * @param dbPassword MongoDB password
     */
    public ProductsUploader(String dbUsername, String dbPassword) {
        super(dbUsername, dbPassword);
    }

    /**
     * Uploads new products from the Zoot and Zalando datasets to the MongoDB collections.
     * The method checks if the products are already uploaded and only uploads new products.
     *
     * @throws IOException if the products could not be deserialized
     * @throws IllegalArgumentException if the connection to MongoDB could not be established
     * using the connection string constructed from the provided username and password
     */
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

    /**
     * Uploads products from the provided JSON file to the MongoDB collection with the given name.
     * @param productsFile
     * @param collectionName
     * @param productType
     * @param <ProductType>
     * @throws IOException
     * @throws MongoSecurityException
     */
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
        System.out.println(
                collection.countDocuments() + " products are currently uploaded in the collection: " + collectionName
        );
        System.out.println(productsToUpload.size() + " new products will be uploaded");

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

    /**
     * Filters out products that are already uploaded to the MongoDB collection.
     * @param productsToUpload
     * @param collection
     * @param <ProductType>
     * @return list of products that are not yet uploaded
     */
    private <ProductType extends DatasetBaseProduct> List<ProductType> filterNewProducts(
            List<ProductType> productsToUpload, MongoCollection<ProductType> collection
    ) {
        Map<String, Boolean> uploadedIds = new HashMap<>();

        collection.distinct("_id", String.class)
                .forEach(id -> uploadedIds.put(id, true));

        var newProducts = productsToUpload.stream()
                .filter(product -> !uploadedIds.containsKey(product.id()));

        return newProducts.collect(Collectors.toList());
    }

    /**
     * Checks if the collection with the given name already exists in the connected MongoDB database.
     * @param collectionName
     * @return
     * @throws MongoSecurityException
     */
    private boolean collectionExists (String collectionName) throws MongoSecurityException {
        System.out.println("Checking if collection already exists: " + collectionName);

        for (String existingName: productsDb.listCollectionNames()) {
            if (existingName.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tries to upload products from the provided JSON file to the MongoDB collection with the given name.
     * @param productsFile
     * @param collectionName
     * @param productType
     * @param <ProductType>
     * @throws IOException if the products could not be deserialized
     * @throws MongoSecurityException
     */
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
