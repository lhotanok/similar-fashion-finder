package org.example.mongodb;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.example.DatasetBaseProduct;
import org.example.ZalandoProduct;
import org.example.ZootProduct;

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

        List<DatasetBaseProduct> products = new ArrayList<>();

        var zootProducts = fetchProductsFromCollection(ids, zootCollection);
        var zalandoProducts = fetchProductsFromCollection(ids, zalandoCollection);

        products.addAll(zootProducts);
        products.addAll(zalandoProducts);

        return products;
    }

    private <ProductType> List<ProductType> fetchProductsFromCollection(
            List<String> ids, MongoCollection<ProductType> collection
    ) {
        Document query = new Document("_id", new Document("$in", ids));

        return collection.find(query)
                .into(new ArrayList<>());
    }
}
