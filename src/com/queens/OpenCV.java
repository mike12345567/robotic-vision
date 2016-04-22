package com.queens;

import com.queens.colours.Colour;
import com.queens.colours.ColourNames;
import com.queens.entities.ColouredArea;
import com.queens.utilities.MatOperations;
import com.queens.utilities.Utilities;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.ArrayList;
import java.util.HashMap;

public class OpenCV {
    private final int thresholdSize = 10;
    private final int kSizeBlur = 3;
    private final int framesBetweenBrightnessCalcs = 30;
    private final int maxPossibleContours = 200;
    private final int captureWidth = 854;
    private final int captureHeight = 480;

    private VideoCapture capture;
    private ArrayList<ColouredArea> areas = new ArrayList<ColouredArea>();
    private ArrayList<Colour> maskColours = new ArrayList<Colour>();
    private Mat lastImage = null;
    private Mat cameraImage = null;
    private Mat displayImage = null;
    private HashMap<ColourNames, Mat> masks = new HashMap<ColourNames, Mat>();
    public Point point = null;
    private int lastBrightnessCalculation = framesBetweenBrightnessCalcs;
    private Colour white = new Colour(ColourNames.White);

    public OpenCV() {
        // deal with a default webcam
        int cameraCode = 1;
        if (!addCamera(cameraCode)) {
            throw new NullPointerException("Camera could not be loaded");
        }
        capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, captureWidth);
        capture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, captureHeight);

        maskColours.add(new Colour(ColourNames.OrangeAndRed));
        maskColours.add(new Colour(ColourNames.Blue));
        maskColours.add(new Colour(ColourNames.Green));
        maskColours.add(new Colour(ColourNames.Yellow));
    }

    /**********************
     *   MAIN FUNCTIONS   *
     **********************/
    public void process() {
        lastImage = cameraImage;
        cameraImage = new Mat();
        Mat toTest = new Mat();
        capture.read(cameraImage);
        cameraImage.copyTo(toTest);
        if (lastBrightnessCalculation >= framesBetweenBrightnessCalcs) {
            lastBrightnessCalculation = 0;
            MatOperations.setBrightnessModeForColours(toTest, maskColours);
        }

        cameraImage = MatOperations.alterBrightness(cameraImage);
        Imgproc.GaussianBlur(cameraImage, cameraImage, new Size(3, 3), 0);
        displayImage = new Mat();
        cameraImage.copyTo(displayImage);
        for (Colour colour : maskColours) {
            Mat mask = mask(cameraImage, colour);
            masks.put(colour.getName(), mask);
            findContours(mask, colour);
        }
        lastBrightnessCalculation++;
    }

    private boolean addCamera(int cameraCode) {
        capture = new VideoCapture(cameraCode);
        if (!capture.isOpened()) {
            if (cameraCode != 0) {
                return addCamera(--cameraCode);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void addColouredAreaOutputs() {
        for (Colour colour : maskColours) {
            MatOperations.drawOutline(displayImage, areas, colour);
            MatOperations.drawPoint(displayImage, point, white);
        }
    }

    public void shutdown() {
        capture.release();
    }

    /**********************
     *       GETTERS      *
     **********************/
    public boolean systemActive() {
        return capture != null && capture.isOpened();
    }

    public ArrayList<ColouredArea> getAreas() {
        return areas;
    }

    public Mat getCameraImage() {
        return cameraImage;
    }

    public Mat getDisplayImage() {
        return displayImage;
    }

    public Mat getLastImage() {
        return lastImage;
    }

    public Mat getMaskImage(ColourNames colourName) {
        return masks.get(colourName);
    }

    /**********************
     *  UTILITY FUNCTIONS *
     **********************/
    private Mat mask(Mat image, Colour colour) {
        Mat hsv_frame = new Mat();

        if (kSizeBlur != 0) {
            Imgproc.medianBlur(image, image, kSizeBlur);
        }

        Imgproc.cvtColor(image, hsv_frame, Imgproc.COLOR_BGR2HSV);

        Mat output = null;
        boolean first = true;
        for (int i = 0; i < colour.getThresholdCount(); i++) {
            Mat mat = new Mat();
            Core.inRange(hsv_frame, colour.getCurrentMinColour(), colour.getCurrentMaxColour(), mat);
            if (first) {
                output = mat;
                first = false;
            } else {
                Core.add(output, mat, output);
            }
        }

        if (output == null) return null;

        Mat dilation = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(output, output, dilation);

        Size size = new Size(7, 7);
        Mat str_el = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, size);
        Imgproc.morphologyEx(output, output, Imgproc.MORPH_OPEN, str_el);
        Imgproc.morphologyEx(output, output, Imgproc.MORPH_CLOSE, str_el);
        return output;
    }

    private void findContours(Mat src, Colour colour) {
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(src.clone(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        int count = contours.size();

        // brightness must have changed
        if (contours.size() > maxPossibleContours) {
            lastBrightnessCalculation = framesBetweenBrightnessCalcs;
            Main.lightingChanged();
            return;
        }

        for (ColouredArea area : areas) {
            if (colour.getName() != area.getColour()) {
                continue;
            }

            Rect closest = null;
            int currentBestDistance = 0;
            int indexContours = 0;
            for (int i = 0; i < count; i++) {
                Rect rect = Utilities.contourToRect(contours.get(i));
                if (rect.size().width > thresholdSize && rect.size().height > thresholdSize) {
                    if (closest == null) {
                        closest = rect;
                    } else {
                        int difference = Utilities.calculateRectDifference(closest, rect);
                        if (difference < currentBestDistance) {
                            currentBestDistance = difference;
                            closest = rect;
                            indexContours = i;
                        }
                    }
                }
            }
            if (closest != null) {
                area.updatePosition(closest);
                area.setInUse(false);
                contours.remove(indexContours);
                count--;
            }
        }

        if (count != 0) {
            for (int i = 0; i < count; i++) {
                Rect rect = Utilities.contourToRect(contours.get(i));
                if (rect.size().width > thresholdSize && rect.size().height > thresholdSize) {
                    ColouredArea area = new ColouredArea(rect, colour.getName());
                    areas.add(area);
                }
            }
        }
    }
}
