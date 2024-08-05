package cz.cuni.mff.mongodb;

import com.mongodb.client.MongoCollection;
import cz.cuni.mff.dataset_products.DatasetBaseProduct;
import cz.cuni.mff.dataset_products.ZalandoProduct;
import org.bson.Document;
import cz.cuni.mff.dataset_products.ZootProduct;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for fetching products from the MongoDB collections "zoot-products" and "zalando-products".
 */
public class ProductsRetrieval extends ProductsDbManager {
    /**
     * Initializes the MongoDB connection with the provided credentials.
     *
     * @param dbUsername MongoDB username
     * @param dbPassword MongoDB password
     */
    public ProductsRetrieval(String dbUsername, String dbPassword) {
        super(dbUsername, dbPassword);
    }

    /**
     * Fetches products from available MongoDB collections - "zoot-products" and "zalando-products".
     * The method retrieves products from Zoot and Zalando collections using their unique IDs
     * and stores them in a common list of {@link DatasetBaseProduct} objects.
     *
     * @param ids list of product IDs to fetch
     * @return list of products with the given IDs
     */
    public List<DatasetBaseProduct> fetchProducts(List<String> ids) {
        System.out.println("Fetching products with IDs: ");
        ids.forEach(System.out::println);

        MongoCollection<ZootProduct> zootCollection = productsDb.getCollection(
                ZOOT_PRODUCTS_COLLECTION, ZootProduct.class);

        MongoCollection<ZalandoProduct> zalandoCollection = productsDb.getCollection(
                ZALANDO_PRODUCTS_COLLECTION, ZalandoProduct.class);

        System.out.println("Loaded all collections");
        List<DatasetBaseProduct> products = new ArrayList<>();

        var zootProducts = fetchProductsFromCollection(ids, zootCollection);
        System.out.printf("Fetched %d products from '%s' collection%n", zootProducts.size(),
                ZOOT_PRODUCTS_COLLECTION);

        var zalandoProducts = fetchProductsFromCollection(ids, zalandoCollection);
        System.out.printf("Fetched %d products from '%s' collection%n", zalandoProducts.size(),
                ZALANDO_PRODUCTS_COLLECTION);

        products.addAll(zootProducts);
        products.addAll(zalandoProducts);

        return products;
    }

    /**
     * Fetches products from a given collection based on their IDs.
     *
     * @param ids list of product IDs to fetch
     * @param collection collection to fetch products from
     * @param <ProductType> type of the products in the collection
     * @return list of products with the given IDs
     */
    private <ProductType> List<ProductType> fetchProductsFromCollection(
            List<String> ids, MongoCollection<ProductType> collection
    ) {
        Document query = new Document("_id", new Document("$in", ids));

        System.out.println("Fetching documents based on IDs with query: " + query.toJson());

        return collection.find(query)
                .into(new ArrayList<>());
    }
}
