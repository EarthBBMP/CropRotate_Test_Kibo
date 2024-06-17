import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;
import java.util.ArrayList;
import java.util.List;

public class Crop {
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

        // get white areas
        Mat thresholded = new Mat();
        Imgproc.threshold(gray, thresholded, 222/*change this will get another result*/, 255, Imgproc.THRESH_BINARY);

        //contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresholded, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //crop the largest contour
        double maxArea = 0;
        MatOfPoint maxContour = new MatOfPoint();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                maxContour = contour;
            }
        }

        // Get the bounding box of the largest contour
        Rect boundingRect = Imgproc.boundingRect(maxContour);

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
        Imgproc.warpPerspective(src, warpedImage, perspectiveTransform, new Size(boundingRect.width, boundingRect.height));

        HighGui.imshow("Warped White Paper", warpedImage);
        HighGui.waitKey();
        System.exit(0);
    }
}
