package org.example;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class App
{
    private static final String DEFAULT_MONGO_DB_USERNAME = "fashion";
    private static final String DEFAULT_MONGO_DB_PASSWORD = "1234";
    private static final String ZOOT_PRODUCTS_COLLECTION = "zoot-products";
    private static final String ZALANDO_PRODUCTS_COLLECTION = "zalando-products";
    private final static URL ZOOT_PRODUCTS_DIR = App.class.getResource("/datasets/zoot");
    private final static URL ZALANDO_PRODUCTS_DIR = App.class.getResource("/datasets/zalando");
    public static void main( String[] args ) throws URISyntaxException {
        System.out.println("Program args: " + Arrays.toString(args));

        String mongoDbUsername = args.length > 0 ? args[0] : DEFAULT_MONGO_DB_USERNAME;
        String mongoDbPassword = args.length > 1 ? args[1] : DEFAULT_MONGO_DB_PASSWORD;

        System.out.println("Mongo DB username: " + mongoDbUsername);
        System.out.println("Mongo DB password: " + mongoDbPassword);

        if (ZOOT_PRODUCTS_DIR == null || ZALANDO_PRODUCTS_DIR == null) {
            System.out.println("Zoot products dir: " + ZOOT_PRODUCTS_DIR);
            System.out.println("Zalando products dir: " + ZALANDO_PRODUCTS_DIR);

            throw new RuntimeException("Dataset directories are not setup correctly." +
                    "Package 'org.example' has to include 'datasets' directory" +
                    "with 'zalando' and 'zoot' subdirectories.");
        }

        var zootFiles = getProductFiles(ZOOT_PRODUCTS_DIR);
        var zalandoFiles = getProductFiles(ZALANDO_PRODUCTS_DIR);

        ProductsUploader uploader = new ProductsUploader(mongoDbUsername, mongoDbPassword);

        zootFiles.forEach(file -> tryUploadProducts(
                uploader, file, ZOOT_PRODUCTS_COLLECTION, ZootProduct.class)
        );

        zalandoFiles.forEach(path -> tryUploadProducts(
                uploader, path, ZALANDO_PRODUCTS_COLLECTION, ZalandoProduct.class)
        );
    }

    private static List<File> getProductFiles (URL productsDirUrl) throws URISyntaxException {
        File productsDir = new File(productsDirUrl.toURI());

        System.out.println("Is '" + productsDir.getName() + "' a directory: " + productsDir.isDirectory());

        File[] productFiles = productsDir.listFiles();

        return productFiles != null
                ? Arrays.stream(productFiles).collect(Collectors.toList())
                : new ArrayList<>();
    }

    private static <ProductType extends MongoDocument> void tryUploadProducts (
            ProductsUploader uploader,
            File productsFile,
            String collectionName,
            Class<ProductType> productType
    ) {
        try {
            uploader.uploadProducts(
                    productsFile,
                    collectionName,
                    productType
            );
        } catch (IOException ex) {
            System.out.println("Products could not be deserialized.");
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }
    }
}
