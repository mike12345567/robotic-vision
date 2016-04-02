package com.queens;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.HashMap;

public class OpenCV {
    private final int thresholdSize = 10;
    private final int maxContrastDiff = 25;
    private final int kSizeBlur = 3;

    private VideoCapture capture;
    private ArrayList<ColouredArea> areas = new ArrayList<ColouredArea>();
    private ArrayList<Colour> maskColours = new ArrayList<Colour>();
    private Mat lastImage = null;
    private Mat cameraImage = null;
    private Mat displayImage = null;
    private HashMap<ColourNames, Mat> masks = new HashMap<ColourNames, Mat>();
    public ArrayList<Point> points = new ArrayList<Point>();

    public OpenCV() {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            throw new NullPointerException("Camera could not be loaded");
        }
        maskColours.add(new Colour(ColourNames.OrangeAndRed));
        //maskColours.add(new Colour(ColourNames.Blue));
        maskColours.add(new Colour(ColourNames.Green));
        //maskColours.add(new Colour(ColourNames.Yellow));
    }

    /**********************
     *   MAIN FUNCTIONS   *
     **********************/
    public void process() {
        lastImage = cameraImage;
        cameraImage = new Mat();
        capture.read(cameraImage);
        alterBrightness(cameraImage);
        displayImage = new Mat();
        cameraImage.copyTo(displayImage);
        for (Colour colour : maskColours) {
            Mat mask = mask(cameraImage, colour);
            masks.put(colour.getName(), mask);
            findContours(mask, colour);
            draw(displayImage, colour);
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
    private void alterBrightness(Mat image) {

        Scalar mean = Core.mean(image);
        double totalMean = (mean.val[0] + mean.val[1] + mean.val[2])/3;
        totalMean -= 50;
        double alpha = 1;
        if (totalMean > maxContrastDiff) {
            totalMean = maxContrastDiff;
        }
        if (totalMean < -maxContrastDiff) {
            totalMean = -maxContrastDiff;
        }
        if (totalMean > 0) {
            alpha -= totalMean / 100;
        } else {
            totalMean *= -2;
        }
        image.convertTo(image, -1, alpha, totalMean);
    }

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

    private void draw(Mat output, Colour colour) {
        for (ColouredArea area : areas) {
            if (area.getColour() == colour.getName()) {
                Rect rect = area.getBoundingBox();
                Imgproc.rectangle(output, rect.tl(), rect.br(), colour.rgbColour, 3);
            }
        }

        for (Point point : points) {
            Rect rect = new Rect((int)point.x, (int)point.y, 5, 5);
            Imgproc.rectangle(output, rect.tl(), rect.br(), new Scalar(255,255,255), 3);
        }
    }
}
