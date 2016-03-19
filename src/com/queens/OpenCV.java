package com.queens;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;

public class OpenCV {
    private final int thresholdSize = 10;
    private final int maxContrastDiff = 25;
    private final int kSizeBlur = 3;

    private VideoCapture capture;
    private ArrayList<ColouredArea> areas = new ArrayList<ColouredArea>();
    private ArrayList<Colour> maskColours = new ArrayList<Colour>();
    private Mat image = null;

    public OpenCV() {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            throw new NullPointerException("Camera could not be loaded");
        }
        maskColours.add(new Colour(ColourNames.Orange));
        //maskColours.add(new Colour(ColourNames.Blue));
        maskColours.add(new Colour(ColourNames.Green));
        //maskColours.add(new Colour(ColourNames.Yellow));
    }

    /**********************
     *   MAIN FUNCTIONS   *
     **********************/
    public void process() {
        image = new Mat();
        capture.read(image);
        alterBrightness(image);
        for (Colour colour : maskColours) {
            Mat mask = mask(image, colour);
            findContours(mask, colour);
            draw(image, colour);
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

    public Mat getImage() {
        return image;
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

        Mat dilation = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6, 6));
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

        for (int i = 0; i < count; i++) {
            MatOfPoint2f point2f = new MatOfPoint2f();
            Imgproc.approxPolyDP(convertTo2f(contours.get(i)), point2f, 3, true);
            Rect rect = Imgproc.boundingRect(convertToP(point2f));
            if (rect.size().width > thresholdSize && rect.size().height > thresholdSize) {
                boolean found = false;
                for (ColouredArea area : areas) {
                    if (colour.getName() != area.getColour()) {
                        continue;
                    }
                    if (area.withinArea(rect) && area.withinSize(rect)) {
                        found = true;
                        area.updatePosition(rect);
                        break;
                    }
                }
                if (!found) {
                    ColouredArea area = new ColouredArea(rect, colour.getName());
                    areas.add(area);
                }
            }
        }
        mergeAreas();
    }

    private void draw(Mat output, Colour colour) {
        for (ColouredArea area : areas) {
            if (area.getColour() == colour.getName()) {
                Rect rect = area.getBoundingBox();
                Imgproc.rectangle(output, rect.tl(), rect.br(), colour.rgbColour, 3);
            }
        }
    }

    private void mergeAreas() {
        ArrayList<ColouredArea> toRemove = new ArrayList<ColouredArea>();
        for (ColouredArea area : areas) {
            for (ColouredArea innerArea : areas) {
                if (area == innerArea || area.getColour() != innerArea.getColour()) {
                    continue;
                }

                int sizeDifference = innerArea.getSize() < area.getSize() ?
                        innerArea.getSize() / area.getSize() :
                        area.getSize() / innerArea.getSize();
                int closeThreshold = innerArea.getWidth() > innerArea.getHeight() ? innerArea.getWidth() : innerArea.getHeight();
                if (sizeDifference < 0.6 && area.close(innerArea.getBoundingBox(), closeThreshold*2)) {
                    if (area.getSize() < innerArea.getSize()) {
                        toRemove.add(area);
                        innerArea.merge(area.getBoundingBox());
                    } else {
                        toRemove.add(innerArea);
                        area.merge(innerArea.getBoundingBox());
                    }
                }
            }
        }
        areas.removeAll(toRemove);
    }

    private static MatOfPoint2f convertTo2f(MatOfPoint point) {
        MatOfPoint2f point2f = new MatOfPoint2f();
        point2f.fromList(point.toList());
        return point2f;
    }

    private static MatOfPoint convertToP(MatOfPoint2f point2f) {
        MatOfPoint point = new MatOfPoint();
        point.fromList(point2f.toList());
        return point;
    }
}
