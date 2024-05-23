package org.example.image_match;

import dev.brachtendorf.jimagehash.datastructures.tree.Result;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.PriorityQueue;

public class ImagesRetrieval extends ImageMatcherDbManager {

    public ImagesRetrieval(String h2Username, String h2Password) throws SQLException {
        super(h2Username, h2Password);
    }
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
