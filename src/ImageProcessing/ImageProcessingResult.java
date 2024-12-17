package ImageProcessing;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class ImageProcessingResult {
    private Mat image;
    private Point center;
    private double radius;

    public ImageProcessingResult(Mat image, Point center, double radius) {
        this.image = image;
        this.center = center;
        this.radius = radius;
    }

    public Mat getImage() {
        return image;
    }

    public Point getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
