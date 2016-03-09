package com.queens;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

public class Main {
    static private ArrayList<ColouredArea> areas = new ArrayList<ColouredArea>();
    static private JFrame window;
    static private ImageIcon image;
    static private JLabel label;
    static private VideoCapture capture;
    static private Colour red = new Colour(ColourNames.Red);
    static private Colour blue = new Colour(ColourNames.Blue);
    static private Colour green = new Colour(ColourNames.Green);
    static private Colour yellow = new Colour(ColourNames.Yellow);
    static private final int thresholdSize = 10;
    static private final int maxContrastDiff = 50;
    static private final int kSizeBlur = 7;
    static private ObjectPairing pairing = new ObjectPairing(ColourNames.Red, ColourNames.Green);
    private static Server server = new Server();
    private static Thread serverThread;

    public static void main(String[] args) {
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            return;
        }

        serverThread = new Thread(server);
        serverThread.start();
        frame();
        loop();
    }

    public static boolean systemActive() {
        return capture != null && capture.isOpened();
    }

    public static void loop() {
        do {
            for (ColouredArea area : areas) {
                area.resetUpdated();
            }
            Mat image = new Mat();
            capture.read(image);
            alterBrightness(image);
            Mat matG = mask(image, green);
            Mat matR = mask(image, red);
            Mat matB = mask(image, blue);
            Mat matY = mask(image, yellow);
            findContours(image, matG, green);
            findContours(image, matR, red);
            findContours(image, matB, blue);
            findContours(image, matY, yellow);
            ArrayList<ColouredArea> toRemove = new ArrayList<ColouredArea>();
            for (int i = 0; i < areas.size(); i++) {
                if (!areas.get(i).hasBeenUpdated()) {
                    toRemove.add(areas.get(i));
                }
            }
            pairing.checkForPairing(areas);
            areas.removeAll(toRemove);
            System.out.printf("ROTATION IS : %f\n", pairing.getRotation());
            show(image);
            try {
                server.putOnQueue("hello");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (systemActive());
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

        Size size = new Size(7, 7);
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
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                capture.release();
            }
        });
    }

    public static void alterBrightness(Mat image) {

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

    public static void findContours(Mat output, Mat src, Colour colour) {
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
                    if (area.withinArea(rect)) {
                        found = true;
                        area.updatePosition(rect);
                        break;
                    }
                }
                if (!found) {
                    ColouredArea area = new ColouredArea(rect, colour.getName());
                    areas.add(area);
                }
                Imgproc.rectangle(output, rect.tl(), rect.br(), colour.rgbColour, 3);
            }
        }
    }

    public static MatOfPoint2f convertTo2f(MatOfPoint point) {
        MatOfPoint2f point2f = new MatOfPoint2f();
        point2f.fromList(point.toList());
        return point2f;
    }

    public static MatOfPoint convertToP(MatOfPoint2f point2f) {
        MatOfPoint point = new MatOfPoint();
        point.fromList(point2f.toList());
        return point;
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
