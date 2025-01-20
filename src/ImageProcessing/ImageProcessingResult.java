package ImageProcessing;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class ImageProcessingResult {
    private static Mat image;
    private static Point center;
    private static double radius;

    public ImageProcessingResult(Mat image, Point center, double radius) {
        ImageProcessingResult.image = image;
        ImageProcessingResult.center = center;
        ImageProcessingResult.radius = radius;
    }

    public static Mat getImage() {
        return image;
    }

    public static Point getCenter() {
        return center;
    }

    public static double getRadius() {
        return radius;
    }
}
