package com.queens.colours;

import org.opencv.core.Scalar;

import java.util.ArrayList;

public class Colour {
    public Scalar rgbColour;
    private ArrayList<Scalar> hsvThresholdMin = new ArrayList<Scalar>();
    private ArrayList<Scalar> hsvThresholdMax = new ArrayList<Scalar>();
    private int current = 0;
    private boolean gotAThreshold = false;
    private ColourNames name;
    private boolean isBright = false;

    public Colour(ColourNames colour) {
        this.name = colour;
        initColourThresholds();
    }

    private void initColourThresholds() {
        hsvThresholdMin.clear();
        hsvThresholdMax.clear();
        int hueMin, hueMax;
        int satMin;
        switch (name) {
            case White: // DO NOT THRESHOLD BY THIS
                rgbColour = new Scalar(255, 255, 255);
                break;
            case OrangeAndRed:
                hueMax = isBright ? 10 : 5;
                rgbColour = new Scalar(128, 128, 255);
                hsvThresholdMin.add(new Scalar(0, 60, 100));        // orange
                hsvThresholdMax.add(new Scalar(hueMax, 255, 255));  // orange
                hsvThresholdMin.add(new Scalar(160, 60, 100));      // red
                hsvThresholdMax.add(new Scalar(179, 255, 255));     // red
                break;
            case Orange:
                hueMin = isBright ? 11 : 6;
                hueMax = isBright ? 18 : 11;
                rgbColour = new Scalar(0, 153, 255);
                hsvThresholdMin.add(new Scalar(hueMin, 100, 180));  // orange
                hsvThresholdMax.add(new Scalar(hueMax, 255, 255));  // orange
                break;
            case Red:
                rgbColour = new Scalar(0, 0, 255);
                hsvThresholdMin.add(new Scalar(160, 80, 80));       // red
                hsvThresholdMax.add(new Scalar(179, 255, 255));     // red
            case Blue:
                satMin = isBright ? 160 : 80;
                rgbColour = new Scalar(255, 0, 0);
                hsvThresholdMin.add(new Scalar(90, 80, satMin));    // blue
                hsvThresholdMax.add(new Scalar(110, 255, 255));     // blue
                break;
            case Green:
                rgbColour = new Scalar(0, 255, 0);
                hsvThresholdMin.add(new Scalar(42, 50, 80));        // green
                hsvThresholdMax.add(new Scalar(69, 255, 255));      // green
                break;
            case Yellow:
                hueMin = isBright ? 22 : 11;
                hueMax = isBright ? 33 : 20;
                rgbColour = new Scalar(0, 255, 255);
                hsvThresholdMin.add(new Scalar(hueMin, 60, 140));   // yellow
                hsvThresholdMax.add(new Scalar(hueMax, 255, 255));  // yellow
                break;
        }
    }

    public void setIsBright(boolean isBright) {
        this.isBright = isBright;
        initColourThresholds();
    }

    public ColourNames getName() {
        return name;
    }

    public int getThresholdCount() {
        return hsvThresholdMax.size();
    }

    public Scalar getCurrentMinColour() {
        Scalar scalar =  hsvThresholdMin.get(current);
        updateCurrent();
        return scalar;
    }

    public Scalar getCurrentMaxColour() {
        Scalar scalar = hsvThresholdMax.get(current);
        updateCurrent();
        return scalar;
    }

    private void updateCurrent() {
        if (gotAThreshold) {
            current++;
            gotAThreshold = false;
        } else {
            gotAThreshold = true;
        }
        if (current >= hsvThresholdMax.size()) {
            gotAThreshold = false;
            current = 0;
        }
    }
}
