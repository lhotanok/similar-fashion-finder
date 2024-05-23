package org.example.image_match;

import javax.imageio.ImageIO;
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
        BufferedImage croppedSquareImage = ImageResizer.cropToSquare(originalImage);
        BufferedImage resizedImage = ImageResizer.resizeSquareImage(croppedSquareImage, SQUARE_IMAGE_SIZE);

        File transformed = new File(image.getPath());
        ImageIO.write(resizedImage, "jpg", transformed);

        return transformed;
    }
}
