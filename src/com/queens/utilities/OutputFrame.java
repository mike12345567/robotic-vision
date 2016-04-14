package com.queens.utilities;

import com.queens.Main;
import com.queens.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;

public class OutputFrame {
    static private boolean testMouseListener = true;
    static private JFrame window;
    static private ImageIcon image;
    static private JLabel label;

    public OutputFrame(){
        window = new JFrame();
        image = new ImageIcon();
        label = new JLabel();
        label.setIcon(image);
        window.getContentPane().setLayout(null);
        window.getContentPane().add(label);
        window.setResizable(false);
        window.setTitle("Robotic Vision");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                Main.shutdown();
            }
        });

        if (testMouseListener) {
            window.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    OpenCV openCV = Main.getOpenCV();
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

                public void mousePressed(MouseEvent e) {}
                public void mouseReleased(MouseEvent e) {}
                public void mouseEntered(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
            });
        }
    }

    public void show(BufferedImage img) {
        image.setImage(img);
        label.setBounds(0, 0, image.getIconWidth(), image.getIconHeight());
        window.pack();
        window.setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
        label.updateUI();
        window.setVisible(true);
    }


    public BufferedImage addLabel(BufferedImage img, String name, int x, int y) {
        return addString(name, img, x, y);
    }

    private BufferedImage addString(String string, BufferedImage old, int x, int y) {
        int w = old.getWidth();
        int h = old.getHeight();
        BufferedImage img = new BufferedImage(
                w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(old, 0, 0, null);
        g2d.setPaint(Color.black);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(string, x, y);
        g2d.dispose();
        return img;
    }


    public BufferedImage toBufferedImage(Mat m) {
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
