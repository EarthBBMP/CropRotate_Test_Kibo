import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;
import java.util.ArrayList;
import java.util.List;

public class CropRotate {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {

        String imagePath = "img/Screenshot_2567-06-17_at_10.34.12.png";
        Mat src = Imgcodecs.imread(imagePath);

        if (src.empty()) {
            System.out.println("Could not load image: " + imagePath);
            return;
        }

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        //get white areas
        Mat thresholded = new Mat();
        Imgproc.threshold(gray, thresholded, 220, 255, Imgproc.THRESH_BINARY);

        //contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresholded, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //largest contour
        double maxArea = 0;
        MatOfPoint maxContour = new MatOfPoint();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                maxContour = contour;
            }
        }

        // Get the minimum area rectangle enclosing the largest contour
        MatOfPoint2f maxContour2f = new MatOfPoint2f(maxContour.toArray());
        RotatedRect rotatedRect = Imgproc.minAreaRect(maxContour2f);

        // Calculate the rotation matrix
        double angle = rotatedRect.angle;
        if (rotatedRect.size.width < rotatedRect.size.height) {
            angle += 90.0;
        }
        Point center = new Point(src.width() / 2, src.height() / 2);
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);

        // Rotate the image
        Mat rotatedImage = new Mat();
        Imgproc.warpAffine(src, rotatedImage, rotationMatrix, src.size(), Imgproc.INTER_LINEAR);

        // Find new bounding box of contour
        Mat rotatedGray = new Mat();
        Imgproc.cvtColor(rotatedImage, rotatedGray, Imgproc.COLOR_BGR2GRAY);
        Mat rotatedThresholded = new Mat();
        Imgproc.threshold(rotatedGray, rotatedThresholded, 220, 255, Imgproc.THRESH_BINARY);

        // Find contours
        List<MatOfPoint> rotatedContours = new ArrayList<>();
        Imgproc.findContours(rotatedThresholded, rotatedContours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //largest contour
        maxArea = 0;
        maxContour = new MatOfPoint();
        for (MatOfPoint contour : rotatedContours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                maxContour = contour;
            }
        }

        //bounding box of the largest contour
        Rect boundingRect = Imgproc.boundingRect(maxContour);

        //อันนี้ให้ chatgpt gen แต่รู้สึกว่า output ไม่เปลี่ยนเลย TT
        // Define the source and destination points for perspective transformation
        Point[] srcPoints = new Point[4];
        srcPoints[0] = new Point(boundingRect.x, boundingRect.y);
        srcPoints[1] = new Point(boundingRect.x + boundingRect.width, boundingRect.y);
        srcPoints[2] = new Point(boundingRect.x + boundingRect.width, boundingRect.y + boundingRect.height);
        srcPoints[3] = new Point(boundingRect.x, boundingRect.y + boundingRect.height);

        Point[] dstPoints = new Point[4];
        dstPoints[0] = new Point(0, 0);
        dstPoints[1] = new Point(boundingRect.width, 0);
        dstPoints[2] = new Point(boundingRect.width, boundingRect.height);
        dstPoints[3] = new Point(0, boundingRect.height);

        MatOfPoint2f srcMat = new MatOfPoint2f(srcPoints);
        MatOfPoint2f dstMat = new MatOfPoint2f(dstPoints);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat);
        Mat warpedImage = new Mat();
        Imgproc.warpPerspective(rotatedImage, warpedImage, perspectiveTransform, new Size(boundingRect.width, boundingRect.height));

        // Display the result using HighGui
        HighGui.imshow("Warped White Paper", warpedImage);
        HighGui.waitKey();
        System.exit(0);
    }
}
