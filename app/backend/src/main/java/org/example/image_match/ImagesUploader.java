package org.example.image_match;

import org.example.DatasetBaseProduct;
import org.example.ZalandoProduct;
import org.example.ZootProduct;
import org.example.dataset_products.DatasetDeserializer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ImagesUploader extends ImageMatcherDbManager {
    private static final int THREADS_FOR_IMAGE_DOWNLOAD = 10;

    public ImagesUploader(String sqlUsername, String sqlPassword) throws SQLException {
        super(sqlUsername, sqlPassword);

        // recreateImageMatcherDb();
    }

    public void uploadProductImages() throws IOException, SQLException, InterruptedException {
        for (File zootFile: DatasetDeserializer.getZootDatasetFiles()) {
            List<ZootProduct> zootProducts = DatasetDeserializer.deserializeJsonCollection(
                    zootFile, ZootProduct.class
            );

            uploadThumbnailHashesToDbParallel(zootProducts);
        }

        for (File zalandoFile: DatasetDeserializer.getZalandoDatasetFiles()) {
            List<ZalandoProduct> zalandoProducts = DatasetDeserializer.deserializeJsonCollection(
                    zalandoFile, ZalandoProduct.class
            );

            uploadThumbnailHashesToDbParallel(zalandoProducts);
        }
    }

    private <ProductType extends DatasetBaseProduct> void uploadThumbnailHashesToDbParallel(List<ProductType> products)
            throws SQLException, IOException, InterruptedException {
        try (ExecutorService executorService = Executors.newFixedThreadPool(THREADS_FOR_IMAGE_DOWNLOAD)) {

            System.out.printf("Downloading thumbnail images of %d products in parallel%n", products.size());

            // List<Future<File>> futures = new ArrayList<>();
            List<Callable<File>> callables = new ArrayList<>();

            for (var product: products) {
                if (product.thumbnail() == null) {
                    continue;
                }

                Callable<File> callable = () -> ImageFileManager.downloadFile(
                        product.thumbnail(),
                        buildTempFilename(product.id())
                );

                callables.add(callable);

                /*Future<File> future = executorService.submit(callable);
                futures.add(future);*/
            }

            List<Future<File>> futures = executorService.invokeAll(callables);
            List<File> downloadedImages = new ArrayList<>();

            for (Future<File> future : futures) {
                try {
                    File downloadedImage = future.get(); // blocking call
                    downloadedImages.add(downloadedImage);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            processDownloadedImages(downloadedImages);

            executorService.shutdown();
        }
    }

    private <ProductType extends DatasetBaseProduct> void uploadThumbnailHashesToDb(List<ProductType> products)
            throws SQLException, IOException, InterruptedException {
        List<File> downloadedImages = new ArrayList<>();

        System.out.printf("Downloading thumbnail images of %d products synchronously%n", products.size());

        for (var product: products) {
            if (product.thumbnail() == null) {
                continue;
            }

            try {
                downloadedImages.add(
                        ImageFileManager.downloadFile(
                                product.thumbnail(),
                                buildTempFilename(product.id())
                        )
                );
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        processDownloadedImages(downloadedImages);
    }

    private void processDownloadedImages(List<File> downloadedImages)
            throws SQLException, IOException, InterruptedException {
        System.out.printf(
                "Uploading hashes for %d images to H2 database '%s'%n",
                downloadedImages.size(),
                ImageMatcherDbManager.DB_NAME
        );

        // It would be more efficient to add all images at once, but it has a tendency to fail and in that case it
        // doesn't upload any images
        /* var imageFiles = downloadedImages.toArray(new File[0]);

        System.out.println("Converted downloaded images to array of length: " + imageFiles.length);
        db.addImages(imageFiles);*/

        for (File image : downloadedImages) {
            try {
                db.addImage(image);
                System.out.println("Uploaded a new image to H2 image matching DB: " + image.getName());
            } catch (Exception e) {
                System.out.printf(
                        "Image '%s' could not be uploaded to H2 image matching DB, message: %s%n",
                        image.getName(),
                        e.getMessage()
                );
            }
        }

        System.out.println("Waiting for 5 seconds before deleting temporarily downloaded image files");
        Thread.sleep(5_000);

        downloadedImages.forEach(ImageFileManager::deleteFile);
        System.out.printf("All temporary files for %d thumbnail files were deleted%n", downloadedImages.size());
    }
}
