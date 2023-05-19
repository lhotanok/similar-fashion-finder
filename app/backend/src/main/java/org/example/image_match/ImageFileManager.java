package org.example.image_match;
import org.apache.commons.io.FileUtils;


import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ImageFileManager {
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

    public static void deleteFile(File localFile) {
        if (localFile.delete()) {
            System.out.println("Local file deleted successfully: " + localFile.getName());
        } else {
            System.out.println("Failed to delete local file: " + localFile.getName());
        }
    }
}
