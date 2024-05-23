package org.example.image_match;

import dev.brachtendorf.jimagehash.hashAlgorithms.*;
import dev.brachtendorf.jimagehash.matcher.persistent.database.H2DatabaseImageMatcher;

import java.sql.SQLException;

public class ImageMatcherDbManager implements AutoCloseable {
    protected static final String DB_NAME = "imageHashDB";
    protected H2DatabaseImageMatcher db;

    private final String h2Username;
    private final String h2Password;
    public ImageMatcherDbManager(String h2Username, String h2Password) throws SQLException {
        this.h2Username = h2Username;
        this.h2Password = h2Password;

        initializeImageMatcherDbConnection();
    }

    @Override
    public void close() throws SQLException {
        System.out.println("Closing connection to MySQL database with images");
        db.close();
    }

    protected void initializeImageMatcherDbConnection() throws SQLException {
        db = new H2DatabaseImageMatcher(DB_NAME, h2Username, h2Password);

        db.clearHashingAlgorithms(true);
        initializeMatchingAlgorithms();
    }

    protected void recreateImageMatcherDb() throws SQLException{
        try(var db = new H2DatabaseImageMatcher(DB_NAME, h2Username, h2Password)) {
            System.out.println("Deleting image hashing database: " + DB_NAME);
            db.deleteDatabase();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        initializeImageMatcherDbConnection();
    }

    private void initializeMatchingAlgorithms() {
        System.out.println(
                "Initializing image matching H2 database with hashing algorithms: " +
                        "DifferenceHash, PerceptiveHash, WaveletHash, RotPHash"
        );

        // threshold closer to 0 is stricter on images

        db.addHashingAlgorithm(new WaveletHash(64, 4), 0.3);
        // db.addHashingAlgorithm(new AverageColorHash(64), 0.20);
        // db.addHashingAlgorithm(new AverageHash(64), 0.45);
        db.addHashingAlgorithm(new DifferenceHash(64, DifferenceHash.Precision.Simple), 0.35);
        db.addHashingAlgorithm(new PerceptiveHash(64), 0.25);
        // db.addHashingAlgorithm(new RotAverageHash(64), 0.5);
        db.addHashingAlgorithm(new RotPHash(64), 0.35);


    }

    protected String buildTempFilename (String filename) {
        return "target/downloaded-images/" + filename + ".jpg";
    }

}
