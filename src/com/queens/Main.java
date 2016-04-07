package com.queens;

import com.queens.colours.ColourNames;
import com.queens.communications.JsonSerializer;
import com.queens.communications.Server;
import com.queens.entities.ColouredArea;
import com.queens.entities.ObjectPairing;
import com.sun.media.sound.InvalidDataException;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

public class Main {
    static private boolean outputEnabled = false;
    static private boolean disableServer = false;
    static private boolean testMouseListener = false;
    static private JFrame window;
    static private ImageIcon image;
    static private JLabel label;

    private static Server server = new Server();
    private static JsonSerializer serializer = new JsonSerializer();
    private static ArrayList<ObjectPairing> robots = new ArrayList<ObjectPairing>();
    private static ObjectPairing pairingTwo = new ObjectPairing("testbot-one", ColourNames.Green, ColourNames.OrangeAndRed);
    private static OpenCV openCV;

    public static void main(String[] args) {
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        openCV = new OpenCV();

        robots.add(new ObjectPairing("testbot-one", ColourNames.Green, ColourNames.OrangeAndRed));
        robots.add(new ObjectPairing("testbot-two", ColourNames.Yellow, ColourNames.Green));
        robots.add(new ObjectPairing("testbot-three", ColourNames.Blue, ColourNames.Yellow));
        robots.add(new ObjectPairing("testbot-four", ColourNames.OrangeAndRed, ColourNames.Blue));

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
            for (ObjectPairing robot : robots) {
                robot.checkForPairing(openCV.getAreas());
            }
            openCV.getAreas().removeAll(toRemove);

            Mat displayImage = openCV.getDisplayImage();
            if (displayImage != null) {
                show(displayImage);
            }

            if (!server.ready()) {
                continue;
            }

            if (disableServer && outputEnabled) {
                for (ObjectPairing robot : robots) {
                    if (!robot.isActive()) continue;
                    float rotation = robot.getRotation();
                    int x = robot.getX();
                    int y = robot.getY();
                    System.out.printf("%s Rotation: %f X: %d Y: %d\n", robot.getPairingName(), rotation, x, y);
                }

            } else if (!disableServer) {
                try {
                    for (ObjectPairing robot : robots) {
                        if (!robot.isActive()) continue;
                        serializer.start();
                        serializer.addSection(robot.getPairingName(), robot);
                        server.putOnQueue(serializer.finish());
                    }
                } catch (InvalidDataException e) {
                    e.printStackTrace();
                }
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

        if (testMouseListener) {
            window.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX() - 6;
                    int y = e.getY() - 27;
                    openCV.points.add(new Point(x, y));
                    Mat mat = new Mat();
                    Imgproc.cvtColor(openCV.getLastImage(), mat, Imgproc.COLOR_BGR2HSV);

                    double array[] = mat.get(y, x);
                    if (array == null) return;

                    for (double number : array) {
                        System.out.printf("%f, ", number);
                    }
                    System.out.println();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
        }
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
