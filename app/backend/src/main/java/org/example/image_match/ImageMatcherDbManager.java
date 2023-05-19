package org.example.image_match;

import dev.brachtendorf.jimagehash.hashAlgorithms.AverageColorHash;
import dev.brachtendorf.jimagehash.hashAlgorithms.AverageHash;
import dev.brachtendorf.jimagehash.hashAlgorithms.DifferenceHash;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import dev.brachtendorf.jimagehash.matcher.persistent.database.H2DatabaseImageMatcher;

import java.sql.SQLException;

public class ImageMatcherDbManager implements AutoCloseable {
    protected static final String DB_NAME = "imageHashDB";
    protected H2DatabaseImageMatcher db;

    public ImageMatcherDbManager(String sqlUsername, String sqlPassword) throws SQLException {
        db = ImageMatcherDbManager.getImageMatcherDbConnection(sqlUsername, sqlPassword);
    }

    @Override
    public void close() throws SQLException {
        System.out.println("Closing connection to MySQL database with images");
        db.close();
    }
    private static H2DatabaseImageMatcher getImageMatcherDbConnection(
            String sqlUsername, String sqlPassword
    ) throws SQLException {
        var db = new H2DatabaseImageMatcher(DB_NAME, sqlUsername, sqlPassword);

        db.clearHashingAlgorithms(true);
        initializeMatchingAlgorithms(db);

        return db;
    }

    private static void initializeMatchingAlgorithms(H2DatabaseImageMatcher db) {
        System.out.println(
                "Initializing image matching MySQL database with hashing algorithms: " +
                        "AverageHash, " +
                        "AverageColorHash, " +
                        "DifferenceHash, " +
                        "PerceptiveHash"
        );

        db.addHashingAlgorithm(new AverageColorHash(64), 0.4);
        db.addHashingAlgorithm(new AverageHash(64), 0.3);
        db.addHashingAlgorithm(new DifferenceHash(64, DifferenceHash.Precision.Simple), 0.4);
        db.addHashingAlgorithm(new PerceptiveHash(64), 0.4);
    }

    protected String buildTempFilename (String filename) {
        return "target/downloaded-images/" + filename + ".jpg";
    }

}
