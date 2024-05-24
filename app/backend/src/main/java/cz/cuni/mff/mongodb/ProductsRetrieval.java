package cz.cuni.mff.mongodb;

import com.mongodb.client.MongoCollection;
import cz.cuni.mff.DatasetBaseProduct;
import cz.cuni.mff.ZalandoProduct;
import org.bson.Document;
import cz.cuni.mff.ZootProduct;

import java.util.ArrayList;
import java.util.List;

public class ProductsRetrieval extends ProductsDbManager {
    public ProductsRetrieval(String dbUsername, String dbPassword) {
        super(dbUsername, dbPassword);
    }

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

    private <ProductType> List<ProductType> fetchProductsFromCollection(
            List<String> ids, MongoCollection<ProductType> collection
    ) {
        Document query = new Document("_id", new Document("$in", ids));

        System.out.println("Fetching documents based on IDs with query: " + query.toJson());

        return collection.find(query)
                .into(new ArrayList<>());
    }
}
