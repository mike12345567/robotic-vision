package com.queens;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

public class Main {
    static private JFrame window;
    static private ImageIcon image;
    static private JLabel label;
    static private VideoCapture capture;
    static private Colour red = new Colour(ColourNames.Red);
    static private Colour blue = new Colour(ColourNames.Blue);
    static private Colour green = new Colour(ColourNames.Green);
    static private final float thresholdRadiusSize = 20f;
    static private final int kSizeBlur = 7;

    public static void main(String[] args) {
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            return;
        }

        frame();
        loop();
    }

    public static void loop() {
        while (true) {
            Mat image = new Mat();
            capture.read(image);
            Mat matG = mask(image, green);
            Mat matR = mask(image, red);
            Mat matB = mask(image, blue);
            findContours(image, matG, green.rgbColour);
            findContours(image, matR, red.rgbColour);
            findContours(image, matB, blue.rgbColour);
            show(image);
        }
    }

    public static Mat mask(Mat image, Colour colour) {
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

        Size size = new Size(3, 3);
        Mat str_el = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, size);
        Imgproc.morphologyEx(output, output, Imgproc.MORPH_OPEN, str_el);
        Imgproc.morphologyEx(output, output, Imgproc.MORPH_CLOSE, str_el);
        return output;
    }

    public static void frame(){
        window = new JFrame();
        image = new ImageIcon();
        label = new JLabel();
        label.setIcon(image);
        window.getContentPane().add(label);
        window.setResizable(false);
        window.setTitle("title");
        setCloseOption(0);
    }

    public static void setCloseOption(int option) {
        switch (option) {
            case 0:
                window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                break;
            case 1:
                window.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                break;
            default:
                window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
    }

    public static void findContours(Mat output, Mat src, Scalar colour) {
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        ArrayList<Point> center = new ArrayList<Point>();
        ArrayList<Integer> radius = new ArrayList<Integer>();

        Imgproc.findContours(src.clone(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        int count = contours.size();

        for (int i = 0; i < count; i++) {
            Point c = new Point();
            float[] r = new float[1];
            Imgproc.minEnclosingCircle(convert(contours.get(i)), c, r);

            if (r[0] > thresholdRadiusSize) {
                center.add(c);
                radius.add((int) r[0]);
            }
        }

        count = center.size();

        for (int i = 0; i < count; i++) {
            Imgproc.circle(output, center.get(i), radius.get(i), colour, 3);
        }
    }

    public static MatOfPoint2f convert(MatOfPoint point) {
        MatOfPoint2f point2f = new MatOfPoint2f();
        point2f.fromList(point.toList());
        return point2f;
    }

    public static void show(Mat img) {
        BufferedImage bufImage;
        try {
            bufImage = toBufferedImage(img);
            image.setImage(bufImage);
            window.pack();
            label.updateUI();
            window.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage toBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster()
                .getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;

    }
}
