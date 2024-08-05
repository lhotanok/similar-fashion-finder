package cz.cuni.mff.image_match;

import dev.brachtendorf.jimagehash.hashAlgorithms.*;
import dev.brachtendorf.jimagehash.matcher.persistent.database.H2DatabaseImageMatcher;

import java.sql.SQLException;

/**
 * This class manages the connection to the H2 database with image hashing algorithms.
 * It initializes the database with the hashing algorithms and provides methods to interact with the database.
 */
public class ImageMatcherDbManager implements AutoCloseable {
    protected static final String DB_NAME = "imageHashDB";
    protected H2DatabaseImageMatcher db;

    private final String h2Username;
    private final String h2Password;

    /**
     * Initializes the connection to the H2 database with the provided username and password.
     *
     * @param h2Username username for the H2 database
     * @param h2Password password for the H2 database
     * @throws SQLException if the connection to the H2 database could not be established
     */
    public ImageMatcherDbManager(String h2Username, String h2Password) throws SQLException {
        this.h2Username = h2Username;
        this.h2Password = h2Password;

        initializeImageMatcherDbConnection();
    }

    /**
     * Closes the connection to the H2 database.
     *
     * @throws SQLException if the connection to the H2 database could not be closed
     */
    @Override
    public void close() throws SQLException {
        System.out.println("Closing connection to MySQL database with images");
        db.close();
    }

    /**
     * Initializes the connection to the H2 database with the provided username and password.
     *
     * @throws SQLException if the connection to the H2 database could not be established
     */
    protected void initializeImageMatcherDbConnection() throws SQLException {
        db = new H2DatabaseImageMatcher(DB_NAME, h2Username, h2Password);

        db.clearHashingAlgorithms(true);
        initializeMatchingAlgorithms();
    }

    /**
     * Initializes the image matching H2 database with hashing algorithms.
     * Currently, the database is initialized with the following hashing algorithms:
     * DifferenceHash, PerceptiveHash, WaveletHash, RotPHash.
     * Other hashing algorithms can be added to the database, such as:
     * AverageColorHash, AverageHash, RotAverageHash.
     */
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

    /**
     * Builds the temporary filename for the downloaded image.
     *
     * @param filename the filename to build the temporary filename from
     * @return the temporary filename
     */
    protected String buildTempFilename (String filename) {
        return "target/downloaded-images/" + filename + ".jpg";
    }

}
