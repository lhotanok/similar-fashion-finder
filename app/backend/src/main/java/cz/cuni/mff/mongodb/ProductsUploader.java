package cz.cuni.mff.mongodb;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoSecurityException;
import com.mongodb.client.MongoCollection;
import cz.cuni.mff.DatasetBaseProduct;
import cz.cuni.mff.ZalandoProduct;
import cz.cuni.mff.ZootProduct;
import cz.cuni.mff.dataset_products.DatasetDeserializer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductsUploader extends ProductsDbManager {
    public ProductsUploader(String dbUsername, String dbPassword) {
        super(dbUsername, dbPassword);
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
