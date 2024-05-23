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
    private static final int THREADPOOL_SIZE = Runtime.getRuntime().availableProcessors();;

    public ImagesUploader(String sqlUsername, String sqlPassword) throws SQLException {
        super(sqlUsername, sqlPassword);

        // recreateImageMatcherDb();
    }

    public void uploadProductImages() throws IOException, SQLException, InterruptedException {
        for (File zootFile: DatasetDeserializer.getZootDatasetFiles()) {
            List<ZootProduct> zootProducts = DatasetDeserializer.deserializeJsonCollection(
                    zootFile, ZootProduct.class
            );

            transformAndSave(
                    downloadThumbnailImagesInParallel(zootProducts)
            );
        }

        for (File zalandoFile: DatasetDeserializer.getZalandoDatasetFiles()) {
            List<ZalandoProduct> zalandoProducts = DatasetDeserializer.deserializeJsonCollection(
                    zalandoFile, ZalandoProduct.class
            );

            transformAndSave(
                    downloadThumbnailImagesInParallel(zalandoProducts)
            );
        }
    }

    private void transformAndSave(List<File> downloadedImages) throws InterruptedException {
        uploadImagesToDb(downloadedImages);

        /*System.out.println("Waiting 5 seconds before deleting temporarily downloaded image files");
        Thread.sleep(5_000);

        downloadedImages.forEach(ImageFileManager::deleteFile);
        System.out.printf("All temporary files for %d thumbnail files were deleted%n", downloadedImages.size());*/
    }

    private <ProductType extends DatasetBaseProduct> List<File> downloadThumbnailImagesInParallel(List<ProductType> products)
            throws InterruptedException {
        try (ExecutorService executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE)) {

            System.out.printf("Downloading thumbnail images of %d products in parallel%n", products.size());

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
            executorService.shutdown();

            return downloadedImages;
        }
    }

    private void uploadImagesToDb(List<File> transformedImages)
            throws InterruptedException {
        System.out.printf(
                "Uploading hashes for %d images to H2 database '%s'%n",
                transformedImages.size(),
                ImageMatcherDbManager.DB_NAME
        );

        System.out.println("Uploading images in parallel using thread pool of size: " + THREADPOOL_SIZE);
        ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);

        for (File image : transformedImages) {
            executor.submit(() -> {
                int retries = 0;
                boolean success = false;

                final int MAX_RETRIES = 3;

                while (retries < MAX_RETRIES && !success) {
                    try {
                        File transformed = ImageTransformation.transform(image);
                        db.addImage(transformed);
                        System.out.println("Uploaded a new image to H2 image matching DB: " + image.getName());
                        success = true;
                    } catch (Exception e) {
                        retries++;
                        System.out.printf(
                                "Image '%s' could not be uploaded to H2 image matching DB, attempt %d, message: %s%n",
                                image.getName(), retries, e.getMessage()
                        );
                        if (retries >= MAX_RETRIES) {
                            System.err.printf("Failed to upload image '%s' after %d attempts.%n", image.getName(), retries);
                        }
                    }
                }
            });
        }

        executor.shutdown();
        boolean imagesUploaded = executor.awaitTermination(2, TimeUnit.HOURS);
        System.out.printf("All images uploaded: %b%n", imagesUploaded);
    }
}
