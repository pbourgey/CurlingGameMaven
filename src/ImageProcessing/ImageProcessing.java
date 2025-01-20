package ImageProcessing;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {

    static {
        // Load OpenCV library
        System.setProperty("java.library.path", "opencv\\build\\java\\x64");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static ImageProcessingResult processImage(Mat image) {
        if (image.empty()) {
            System.out.println("Error: The provided image is empty.");
            return new ImageProcessingResult(image, null, 0);
        }


        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply Gaussian blur to reduce noise
        Imgproc.GaussianBlur(gray, gray, new Size(9, 9), 2, 2);

        // Detect circles using HoughCircles
        Mat circles = new Mat();
        Imgproc.HoughCircles(
                gray,
                circles,
                Imgproc.HOUGH_GRADIENT,
                1.0,
                (double) gray.rows() / 8, // Minimum distance between circle centers
                100.0, // Upper threshold for the Canny edge detector
                30.0,  // Accumulator threshold for circle centers
                10,    // Minimum radius
                100    // Maximum radius
        );

        // Find the most "circular" circle
        double maxRoundness = 0;
        Point bestCenter = null;
        double bestRadius = 0;

        for (int i = 0; i < circles.cols(); i++) {
            double[] data = circles.get(0, i);
            if (data == null) continue;

            double centerX = data[0];
            double centerY = data[1];
            double radius = data[2];

            // Check "circularity": here, we use the radius as an approximation
            double roundness = Math.PI * radius * radius;
            if (roundness > maxRoundness) {
                maxRoundness = roundness;
                bestCenter = new Point(centerX, centerY);
                bestRadius = radius;
            }
        }

        // Draw the best circle found on the original image
        if (bestCenter != null) {
            Imgproc.circle(image, bestCenter, (int) bestRadius, new Scalar(0, 255, 0), 3);
            Imgproc.rectangle(
                    image,
                    new Point(bestCenter.x - bestRadius, bestCenter.y - bestRadius),
                    new Point(bestCenter.x + bestRadius, bestCenter.y + bestRadius),
                    new Scalar(255, 0, 0),
                    2
            );
            // System.out.println("Most circular circle found: Center = " + bestCenter + ", Radius = " + bestRadius);
        } else {
            //System.out.println("No circles detected.");
        }

        // Return the resulting image (with the circle and cross drawn)
        return new ImageProcessingResult(image, bestCenter, bestRadius);
    }

}