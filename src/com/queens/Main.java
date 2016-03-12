package com.queens;

import com.sun.media.sound.InvalidDataException;
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

    private static Server server = new Server();
    private static JsonSerializer serializer = new JsonSerializer();
    private static ObjectPairing pairing = new ObjectPairing(ColourNames.Red, ColourNames.Green);
    private static OpenCV openCV;

    public static void main(String[] args) {
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        openCV = new OpenCV();

        frame();
        loop();
    }

    public static boolean currentlyRunning() {
        return openCV.systemActive();
    }

    public static void loop() {
        do {
            for (ColouredArea area : openCV.getAreas()) {
                area.resetUpdated();
            }

            openCV.process();
            ArrayList<ColouredArea> toRemove = new ArrayList<ColouredArea>();
            for (int i = 0; i < openCV.getAreas().size(); i++) {
                if (!openCV.getAreas().get(i).hasBeenUpdated()) {
                    toRemove.add(openCV.getAreas().get(i));
                }
            }
            pairing.checkForPairing(openCV.getAreas());
            openCV.getAreas().removeAll(toRemove);
            show(openCV.getImage());
            try {
                serializer.start();
                serializer.addSection(pairing);
                server.putOnQueue(serializer.finish());
            } catch (InvalidDataException e) {
                e.printStackTrace();
            }
        } while (currentlyRunning());
    }


    public static void frame(){
        window = new JFrame();
        image = new ImageIcon();
        label = new JLabel();
        label.setIcon(image);
        window.getContentPane().add(label);
        window.setResizable(false);
        window.setTitle("Robotic Vision");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                openCV.shutdown();
                server.shutdown();
            }
        });
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
