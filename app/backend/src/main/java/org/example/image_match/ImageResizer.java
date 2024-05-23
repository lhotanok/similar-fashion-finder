package org.example.image_match;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageResizer {
    public static BufferedImage resizeSquareImage(BufferedImage originalImage, int targetSize) {
        Image resultingImage = originalImage.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();

        return outputImage;
    }

    public static BufferedImage cropToSquare(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Determine the cropping rectangle
        int cropSize = Math.min(originalWidth, originalHeight);
        int x = (originalWidth - cropSize) / 2;
        int y = (originalHeight - cropSize) / 2;

        return originalImage.getSubimage(x, y, cropSize, cropSize);
    }
}
