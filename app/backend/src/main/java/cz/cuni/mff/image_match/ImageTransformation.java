package cz.cuni.mff.image_match;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Class for transforming images to a square dimension and resizing them.
 * Images resized to a size of 256×256 pixels are more efficient for image matching.
 */
public class ImageTransformation {
    /**
     * The size of the square image in pixels.
     */
    private static final int SQUARE_IMAGE_SIZE = 256;

    /**
     * Transforms the provided image to a square image of size {@link #SQUARE_IMAGE_SIZE}.
     * The image is first cropped to a square and then resized to the target size.
     * @param image the image to crop and resize
     * @return the transformed image
     * @throws IOException if the buffered image could not be read or written
     */
    public static File transform(File image) throws IOException {
        System.out.printf(
                "Transforming image to size %d×%d (%s)%n",
                SQUARE_IMAGE_SIZE,
                SQUARE_IMAGE_SIZE,
                image.getName()
        );

        BufferedImage originalImage = ImageIO.read(image);
        BufferedImage croppedSquareImage = cropToSquare(originalImage);
        BufferedImage resizedImage = resizeSquareImage(croppedSquareImage, SQUARE_IMAGE_SIZE);

        File transformed = new File(image.getPath());
        ImageIO.write(resizedImage, "jpg", transformed);

        return transformed;
    }

    /**
     * Resizes the provided square image to a target size.
     * The function assumes the image is square. If you need to resize a non-square image,
     * use the {@link #cropToSquare(BufferedImage)} function first.
     * @param squareImage the image to resize
     * @param targetSize the target size of the square image
     * @return the resized image
     */
    private static BufferedImage resizeSquareImage(BufferedImage squareImage, int targetSize) {
        Image resultingImage = squareImage.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();

        return outputImage;
    }

    /**
     * Crops the provided image to a square.
     * @param originalImage the image to crop
     * @return the cropped square image
     */
    private static BufferedImage cropToSquare(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Determine the cropping rectangle
        int cropSize = Math.min(originalWidth, originalHeight);
        int x = (originalWidth - cropSize) / 2;
        int y = (originalHeight - cropSize) / 2;

        return originalImage.getSubimage(x, y, cropSize, cropSize);
    }
}
