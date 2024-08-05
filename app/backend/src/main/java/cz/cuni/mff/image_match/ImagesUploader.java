package cz.cuni.mff.image_match;

import cz.cuni.mff.dataset_products.DatasetBaseProduct;
import cz.cuni.mff.dataset_products.ZalandoProduct;
import cz.cuni.mff.dataset_products.ZootProduct;
import cz.cuni.mff.dataset_products.DatasetDeserializer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Class for downloading and uploading images to the H2 database.
 */
public class ImagesUploader extends ImageMatcherDbManager {
    private static final int THREADPOOL_SIZE = Runtime.getRuntime().availableProcessors();;

    /**
     * Initializes the connection to the H2 database with the provided username and password.
     *
     * @param sqlUsername username for the H2 database
     * @param sqlPassword password for the H2 database
     * @throws SQLException if the connection to the H2 database could not be established
     */
    public ImagesUploader(String sqlUsername, String sqlPassword) throws SQLException {
        super(sqlUsername, sqlPassword);
    }

    /**
     * Downloads thumbnail images of products from the Zoot and Zalando datasets in parallel.
     * The downloaded images are transformed and uploaded to the H2 database.
     *
     * @throws IOException if the image could not be downloaded
     * @throws SQLException if the connection to the H2 database could not be established
     * @throws InterruptedException if the thread is interrupted while waiting for the image to be uploaded
     */
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

    /**
     * Transforms and saves the downloaded images to the H2 database.
     * @param downloadedImages the list of downloaded images to be transformed and saved. Images are stored
     *                         in temporary files and they can be deleted once they are uploaded to the database.
     * @throws InterruptedException if the thread is interrupted while waiting for the image to be uploaded
     */
    private void transformAndSave(List<File> downloadedImages) throws InterruptedException {
        uploadImagesToDb(downloadedImages);

        /*System.out.println("Waiting 5 seconds before deleting temporarily downloaded image files");
        Thread.sleep(5_000);

        downloadedImages.forEach(ImageFileManager::deleteFile);
        System.out.printf("All temporary files for %d thumbnail files were deleted%n", downloadedImages.size());*/
    }

    /**
     * Downloads thumbnail images of products in parallel.
     * The downloaded images are stored in temporary files.
     *
     * @param products the list of products to download thumbnail images for
     * @param <ProductType> the type of the product
     * @return the list of images downloaded into temporary files
     * @throws InterruptedException if the thread is interrupted while waiting for the image to be downloaded
     */
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

    /**
     * Uploads the transformed images to the H2 database.
     *
     * @param transformedImages the list of transformed images to upload to the H2 database
     * @throws InterruptedException if the thread is interrupted while waiting for the image to be uploaded
     */
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
