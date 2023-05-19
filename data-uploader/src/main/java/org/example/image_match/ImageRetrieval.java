package org.example.image_match;

import dev.brachtendorf.jimagehash.datastructures.tree.Result;

import java.io.IOException;
import java.sql.SQLException;
import java.util.PriorityQueue;

public class ImageRetrieval extends ImageMatcherDbManager {

    public ImageRetrieval(String sqlUsername, String sqlPassword) throws SQLException {
        super(sqlUsername, sqlPassword);
    }
    public PriorityQueue<Result<String>> getMatchingImages(String imageRemoteUrl) throws SQLException, IOException {
        var exampleImageToBeMatched = ImageFileManager.downloadFile(
                imageRemoteUrl,
                buildTempFilename("image-to-match")
        );

        PriorityQueue<Result<String>> results = db.getMatchingImages(exampleImageToBeMatched);

        System.out.printf("Matched %d images for '%s':%n", results.size(), imageRemoteUrl);
        results.forEach(System.out::println);

        return results;
    }
}
