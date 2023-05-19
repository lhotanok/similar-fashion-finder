package org.example.image_match;

import dev.brachtendorf.jimagehash.hashAlgorithms.AverageColorHash;
import dev.brachtendorf.jimagehash.hashAlgorithms.AverageHash;
import dev.brachtendorf.jimagehash.matcher.persistent.database.H2DatabaseImageMatcher;
import org.example.DatasetBaseProduct;
import org.example.ZalandoProduct;
import org.example.ZootProduct;
import org.example.dataset_products.DatasetDeserializer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImagesUploader implements AutoCloseable {
    private static final String DB_NAME = "images";

    private final H2DatabaseImageMatcher db;

    public ImagesUploader(String sqlUsername, String sqlPassword) throws SQLException {
        db = new H2DatabaseImageMatcher(DB_NAME, sqlUsername, sqlPassword);

        System.out.println(
                "Initializing image matching MySQL database with hashing algorithms: AverageHash, AverageColorHash"
        );
        db.addHashingAlgorithm(new AverageColorHash(32), .4);
        db.addHashingAlgorithm(new AverageHash(32), .10);
    }

    public void uploadProductImages() throws IOException, SQLException, InterruptedException {
        for (File zootFile: DatasetDeserializer.getZootDatasetFiles()) {
            List<ZootProduct> zootProducts = DatasetDeserializer.deserializeJsonCollection(
                    zootFile, ZootProduct.class
            );

            uploadThumbnailHashesToDb(zootProducts);
        }

        for (File zalandoFile: DatasetDeserializer.getZalandoDatasetFiles()) {
            List<ZalandoProduct> zalandoProducts = DatasetDeserializer.deserializeJsonCollection(
                    zalandoFile, ZalandoProduct.class
            );

            uploadThumbnailHashesToDb(zalandoProducts);
        }
    }

    @Override
    public void close() throws SQLException {
        System.out.println("Closing connection to MySQL database with images");
        db.close();
    }

    private <ProductType extends DatasetBaseProduct> void uploadThumbnailHashesToDb(List<ProductType> products)
            throws SQLException, IOException, InterruptedException {
        List<File> downloadedImages = new ArrayList<>();

        System.out.printf("Downloading thumbnail images of %d products%n", products.size());

        for (var product: products) {
            String tempFileName = "target/downloaded-images/" + UUID.randomUUID() + ".jpg";
            String thumbnail = product.thumbnail();

            if (thumbnail == null) {
                continue;
            }

            downloadedImages.add(
                    ImageFileManager.downloadFile(thumbnail, tempFileName)
            );
        }

        System.out.printf(
                "Uploading hashes for %d images to MySQL database '%s'%n",
                downloadedImages.size(),
                DB_NAME
        );

        db.addImages(downloadedImages.toArray(new File[0]));

        System.out.println("Waiting for 30 seconds before deleting temporarily downloaded image files");
        Thread.sleep(30_000);

        downloadedImages.forEach(ImageFileManager::deleteFile);
        System.out.printf("All temporary files for %d thumbnail files were deleted%n", products.size());
    }
}
