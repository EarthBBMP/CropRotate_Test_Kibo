import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class Nocrop {
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
        Imgproc.threshold(gray, thresholded, 225, 255, Imgproc.THRESH_BINARY);

        //contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresholded, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Draw contours
        Mat result = src.clone();
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(result, contours, i, new Scalar(0, 255, 0), 2);
        }

        HighGui.imshow("Detected White Paper", result);
        HighGui.waitKey();
        System.exit(0);
    }
}
