package cz.cuni.mff.image_match;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageTransformation {
    private static final int SQUARE_IMAGE_SIZE = 256;

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

    private static BufferedImage resizeSquareImage(BufferedImage originalImage, int targetSize) {
        Image resultingImage = originalImage.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();

        return outputImage;
    }

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
