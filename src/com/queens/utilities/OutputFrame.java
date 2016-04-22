package com.queens.utilities;

import com.queens.Main;
import com.queens.communications.Server;
import com.queens.entities.FactorBtnListener;
import com.queens.entities.MyMouseListener;
import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class OutputFrame {
    static private boolean testMouseListener = true;
    static private JFrame window;
    static private ImageIcon image;
    static private JLabel label;
    static private JLabel alphaLbl, betaLbl;
    static private JButton minusAlpha, plusAlpha, minusBeta, plusBeta;

    public OutputFrame(Server server){
        window = new JFrame();
        image = new ImageIcon();
        label = new JLabel();
        label.setIcon(image);
        window.setLayout(null);
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

        setButtonBounds(800, 400);

        if (testMouseListener) {
            window.addMouseListener(new MyMouseListener(server));
        }
        window.setVisible(true);
    }

    public void show(BufferedImage img) {
        image.setImage(img);
        label.setBounds(0, 0, image.getIconWidth(), image.getIconHeight());
        window.pack();
        window.setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()+100));
        setButtonBounds(image.getIconWidth()/2-100, image.getIconHeight());
        label.updateUI();
    }

    private void setButtonBounds(int x, int y) {
        y += 10;
        x += 10;
        int width = 60, height = 24;

        if (alphaLbl == null || betaLbl == null) {
            minusAlpha = new JButton("-");
            alphaLbl = new JLabel();
            plusAlpha = new JButton("+");
            minusBeta = new JButton("-");
            betaLbl = new JLabel();
            plusBeta = new JButton("+");
            window.add(minusAlpha);
            window.add(alphaLbl);
            window.add(plusAlpha);
            window.add(minusBeta);
            window.add(betaLbl);
            window.add(plusBeta);
            resetBrightnessLabels();

            FactorBtnListener alphaListener = new FactorBtnListener(true, alphaLbl);
            FactorBtnListener betaListener = new FactorBtnListener(false, betaLbl);
            minusAlpha.addActionListener(alphaListener);
            plusAlpha.addActionListener(alphaListener);
            minusBeta.addActionListener(betaListener);
            plusBeta.addActionListener(betaListener);
        }

        
        minusAlpha.setBounds(x, y, width, height);
        alphaLbl.setBounds(Utilities.getNewX(minusAlpha), y, width, height);
        plusAlpha.setBounds(Utilities.getNewX(alphaLbl), y, width, height);

        y = minusAlpha.getY() + minusAlpha.getHeight() + 10;
        minusBeta.setBounds(x, y, width, height);
        betaLbl.setBounds(Utilities.getNewX(minusBeta), y, width, height);
        plusBeta.setBounds(Utilities.getNewX(betaLbl), y, width, height);
    }

    public void resetBrightnessLabels() {
        alphaLbl.setText("Alpha: " + MatOperations.alphaFactor);
        betaLbl.setText("Beta: " + MatOperations.betaFactor);
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
