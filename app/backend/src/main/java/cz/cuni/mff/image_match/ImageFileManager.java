package cz.cuni.mff.image_match;
import org.apache.commons.io.FileUtils;


import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Class for downloading and deleting image files.
 */
public class ImageFileManager {
    /**
     * Downloads a file from the provided remote URL to the local file.
     *
     * @param remoteFileUrl the URL of the remote file to download
     * @param localFilepath the local file path to save the downloaded file to
     * @return the downloaded file
     * @throws IllegalArgumentException if the file could not be downloaded
     */
    public static File downloadFile(String remoteFileUrl, String localFilepath) {
        try {
            File downloadedFile = new File(localFilepath);

            FileUtils.copyURLToFile(
                    new URL(remoteFileUrl),
                    downloadedFile
            );

            System.out.println(
                    String.format("Downloaded file '%s', can read: ", localFilepath) + downloadedFile.canRead()
            );

            return downloadedFile;
        } catch (IOException e) {
            String errorMessage = "File could not be downloaded." + "Invalid remote file URL: " + remoteFileUrl;
            System.out.println(errorMessage);

            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Deletes the provided local file.
     *
     * @param localFile the local file to delete
     */
    public static void deleteFile(File localFile) {
        if (localFile.delete()) {
            System.out.println("Local file deleted successfully: " + localFile.getName());
        } else {
            System.out.println("Failed to delete local file: " + localFile.getName());
        }
    }
}
