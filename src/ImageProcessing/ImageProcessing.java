package ImageProcessing;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessing {
    static {
        // Load OpenCV library
        System.setProperty("java.library.path", "C:\\opencv\\opencv\\build\\java\\x64");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Mat img;
    private Mat grayImg;

  
    public ImageProcessing(String imagePath) {
        this.img = Imgcodecs.imread(imagePath);
        this.grayImg = new Mat();
        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
    }

    
    public ImageProcessing(Mat mat) {
        this.img = mat;
        this.grayImg = new Mat();
        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
    }
  
    public Mat preprocessImage() {
        Mat binaryImg = new Mat();
        Imgproc.threshold(grayImg, binaryImg, 120, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite("thresh.jpg", binaryImg);
        Imgproc.morphologyEx(binaryImg, binaryImg, Imgproc.MORPH_CLOSE,
        		Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(10, 10)));
        Imgcodecs.imwrite("close.jpg", binaryImg);
        return binaryImg;
    }


    public MatOfPoint findLargestContour(Mat binaryImg) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binaryImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = 0;
        MatOfPoint largestContour = null;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                largestContour = contour;
            }
        }
        return largestContour;
    }

    public Point[] findCorners(MatOfPoint largestContour) {
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(largestContour, hull);

        List<Point> hullPoints = new ArrayList<>();
        for (int i : hull.toArray()) {
            hullPoints.add(largestContour.toList().get(i));
        }

   
        Point center = new Point(0, 0);
        for (Point p : hullPoints) {
            center.x += p.x;
            center.y += p.y;
        }
        center.x /= hullPoints.size();
        center.y /= hullPoints.size();

    
        Point topLeft = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
        Point topRight = new Point(Double.MIN_VALUE, Double.MAX_VALUE);
        Point bottomLeft = new Point(Double.MAX_VALUE, Double.MIN_VALUE);
        Point bottomRight = new Point(Double.MIN_VALUE, Double.MIN_VALUE);

        for (Point p : hullPoints) {
            if (p.x <= center.x && p.y <= center.y && p.y < topLeft.y) topLeft = p;
            if (p.x > center.x && p.y <= center.y && p.y < topRight.y) topRight = p;
            if (p.x <= center.x && p.y > center.y && p.y > bottomLeft.y) bottomLeft = p;
            if (p.x > center.x && p.y > center.y && p.y > bottomRight.y) bottomRight = p;
        }
        return new Point[]{topLeft, topRight, bottomLeft, bottomRight};
    }

    public Mat warpImage(Point[] corners) {
        MatOfPoint2f srcPoints = new MatOfPoint2f(corners);
        MatOfPoint2f dstPoints = new MatOfPoint2f(
                new Point(0, 0),
                new Point(2970, 0),
                new Point(0, 2100),
                new Point(2970, 2100)
        );
        Mat transform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);

        Mat warpedImg = new Mat();
        Imgproc.warpPerspective(img, warpedImg, transform, new Size(2970, 2100));
        return warpedImg;
    }


    public double detectObjectsAndCalculateDistance(Mat warpedImg) {
        Mat binarizedImg = new Mat();
        Imgproc.adaptiveThreshold(grayImg, binarizedImg, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY_INV, 11, 2);

        List<MatOfPoint> objectContours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binarizedImg, objectContours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

     
        objectContours.sort((c1, c2) -> Double.compare(Imgproc.contourArea(c2), Imgproc.contourArea(c1)));

        if (objectContours.size() < 2) {
            throw new IllegalStateException("Not enough objects detected.");
        }

        Point jetonCenter = Imgproc.boundingRect(objectContours.get(0)).tl();
        Point cibleCenter = Imgproc.boundingRect(objectContours.get(1)).tl();

   
        return Math.sqrt(Math.pow(jetonCenter.x - cibleCenter.x, 2) + Math.pow(jetonCenter.y - cibleCenter.y, 2));
    }

 
    public static void main(String[] args) {

        ImageProcessing processor = new ImageProcessing("C:\\info_telecom\\proj_info_fise2\\hockeyAR\\nice_capture\\captured18.jpg");

        Mat binaryImg = processor.preprocessImage();
        MatOfPoint largestContour = processor.findLargestContour(binaryImg);
        Point[] corners = processor.findCorners(largestContour);
        Mat warpedImg = processor.warpImage(corners);
        Imgcodecs.imwrite("result.jpg", warpedImg);
        double distance = processor.detectObjectsAndCalculateDistance(warpedImg);
        System.out.println("Distance between objects: " + distance + " pixels");
        
    }
}