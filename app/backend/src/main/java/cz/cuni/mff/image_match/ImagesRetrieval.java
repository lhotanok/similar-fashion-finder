package cz.cuni.mff.image_match;

import dev.brachtendorf.jimagehash.datastructures.tree.Result;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.PriorityQueue;

/**
 * Class for retrieving images from the database and matching them with the provided image.
 */
public class ImagesRetrieval extends ImageMatcherDbManager {
    /**
     * Initializes the connection to the H2 database with the provided username and password.
     *
     * @param h2Username username for the H2 database
     * @param h2Password password for the H2 database
     * @throws SQLException if the connection to the H2 database could not be established
     */
    public ImagesRetrieval(String h2Username, String h2Password) throws SQLException {
        super(h2Username, h2Password);
    }

    /**
     * Retrieves matching images from the database for the provided image URL.
     *
     * @param imageRemoteUrl the URL of the image to match. This image is downloaded and transformed before matching.
     * @return a priority queue of matching images.
     * The queue is sorted by the similarity of the images to the query image.
     * @throws SQLException if the connection to the H2 database could not be established
     * @throws IOException  if the image could not be downloaded
     */
    public PriorityQueue<Result<String>> getMatchingImages(String imageRemoteUrl) throws SQLException, IOException {
        PriorityQueue<Result<String>> results = new PriorityQueue<>();

        try {
            File exampleImageToBeMatched = ImageFileManager.downloadFile(
                    imageRemoteUrl,
                    buildTempFilename("image-to-match")
            );

            File transformedImage = ImageTransformation.transform(exampleImageToBeMatched);
            results = db.getMatchingImages(transformedImage);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.printf("Matched %d images for '%s':%n", results.size(), imageRemoteUrl);
        results.forEach(System.out::println);

        return results;
    }
}
