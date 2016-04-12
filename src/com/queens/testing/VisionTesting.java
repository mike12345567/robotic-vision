package com.queens.testing;

import org.opencv.core.Rect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VisionTesting {
    private JFrame frame;
    private JPanel mainPanel;
    private JButton startButton;
    private JButton stopButton;
    private JTextField xField;
    private JTextField yField;
    private JTextField rotationField;
    private JTextField[] xHazFields = new JTextField[2], yHazFields = new JTextField[2],
                         widthHazFields = new JTextField[2], heightHazFields = new JTextField[2];

    private boolean isRunning;

    public VisionTesting() {
        setupUI();

        frame = new JFrame("Vision Testing");
        frame.setPreferredSize(mainPanel.getPreferredSize());
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isRunning = true;
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isRunning = false;
            }
        });

        setData();
    }

    private void setupUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        mainPanel.setPreferredSize(new Dimension(400, 230));
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        topPanel.setPreferredSize(new Dimension(400, 40));
        mainPanel.add(topPanel);

        JLabel xLbl = new JLabel();
        xLbl.setText("x loc");
        topPanel.add(xLbl);
        xField = new JTextField();
        xField.setPreferredSize(new Dimension(50, 24));
        topPanel.add(xField);


        JLabel yLbl = new JLabel();
        yLbl.setText("y loc");
        topPanel.add(yLbl);
        yField = new JTextField();
        yField.setPreferredSize(new Dimension(50, 24));
        topPanel.add(yField);

        JLabel rotationLbl = new JLabel();
        rotationLbl.setText("rotation");
        topPanel.add(rotationLbl);
        rotationField = new JTextField();
        rotationField.setPreferredSize(new Dimension(50, 24));
        topPanel.add(rotationField);

        for (int i = 0; i < 2; i++) {
            JPanel midPanel = new JPanel();
            JLabel hazard = new JLabel();
            hazard.setText("hazard" + (i + 1));
            midPanel.add(hazard);
            midPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            midPanel.setPreferredSize(new Dimension(400, 40));
            xHazFields[i] = new JTextField();
            xHazFields[i].setPreferredSize(new Dimension(40, 24));
            yHazFields[i] = new JTextField();
            yHazFields[i].setPreferredSize(new Dimension(40, 24));
            widthHazFields[i] = new JTextField();
            widthHazFields[i].setPreferredSize(new Dimension(40, 24));
            heightHazFields[i] = new JTextField();
            heightHazFields[i].setPreferredSize(new Dimension(40, 24));
            midPanel.add(xHazFields[i]);
            midPanel.add(yHazFields[i]);
            midPanel.add(widthHazFields[i]);
            midPanel.add(heightHazFields[i]);
            mainPanel.add(midPanel);
        }

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        bottomPanel.setPreferredSize(new Dimension(400, 40));
        mainPanel.add(bottomPanel);
        startButton = new JButton();
        startButton.setText("Start");
        bottomPanel.add(startButton);
        stopButton = new JButton();
        stopButton.setText("Stop");
        bottomPanel.add(stopButton);
    }

    private void setData() {
        xField.setText("100");
        yField.setText("100");
        rotationField.setText("100");
        for (int i = 0; i < 2; i++) {
            xHazFields[i].setText("100");
            yHazFields[i].setText("100");
            widthHazFields[i].setText("100");
            heightHazFields[i].setText("100");
        }
    }

    public int getXLocation() {
        return Integer.parseInt(xField.getText());
    }

    public int getYLocation() {
        return Integer.parseInt(yField.getText());
    }

    public float getRotation() {
        return Float.parseFloat(rotationField.getText());
    }

    public Rect getHazardRect(int hazardNo) {
        int x = Integer.parseInt(xHazFields[hazardNo].getText());
        int y = Integer.parseInt(yHazFields[hazardNo].getText());
        int width = Integer.parseInt(widthHazFields[hazardNo].getText());
        int height = Integer.parseInt(heightHazFields[hazardNo].getText());
        return new Rect(x, y, width, height);
    }

    public boolean isVisible() {
        return frame.isVisible();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
