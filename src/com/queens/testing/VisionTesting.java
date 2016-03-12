package com.queens.testing;

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
    }

    private void setupUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        mainPanel.setPreferredSize(new Dimension(400, 150));
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

    public int getXLocation() {
        return Integer.parseInt(xField.getText());
    }

    public int getYLocation() {
        return Integer.parseInt(yField.getText());
    }

    public float getRotation() {
        return Float.parseFloat(rotationField.getText());
    }

    public boolean isVisible() {
        return frame.isVisible();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
