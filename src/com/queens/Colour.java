package com.queens;

import org.opencv.core.Scalar;

import java.util.ArrayList;

public class Colour {
    public Scalar rgbColour;
    private ArrayList<Scalar> hsvThresholdMin = new ArrayList<Scalar>();
    private ArrayList<Scalar> hsvThresholdMax = new ArrayList<Scalar>();
    int current = 0;
    boolean gotAThreshold = false;
    ColourNames name;

    public Colour(ColourNames colour) {
        this.name = colour;
        switch (colour) {
            case Orange:
                rgbColour = new Scalar(128, 128, 255);
                hsvThresholdMin.add(new Scalar(0, 80, 80));     // orange
                hsvThresholdMax.add(new Scalar(16, 255, 255));  // orange
            break;
            case Red:
                rgbColour = new Scalar(0, 0, 255);
                hsvThresholdMin.add(new Scalar(160, 80, 80));   // red
                hsvThresholdMax.add(new Scalar(179, 255, 255)); // red
            case Blue:
                rgbColour = new Scalar(255, 0, 0);
                hsvThresholdMin.add(new Scalar(75, 50, 50));    // blue and violet
                hsvThresholdMax.add(new Scalar(160, 255, 255)); // blue and violet
                break;
            case SpecificBlue:
                rgbColour = new Scalar(255, 0, 0);
                hsvThresholdMin.add(new Scalar(100, 50, 50));
                hsvThresholdMax.add(new Scalar(105, 255, 255));
                break;
            case Green:
                rgbColour = new Scalar(0, 255, 0);
                hsvThresholdMin.add(new Scalar(39, 70, 70));    // green
                hsvThresholdMax.add(new Scalar(75, 255, 255));  // green
                break;
            case SpecificGreen:
                rgbColour = new Scalar(0, 255, 0);
                hsvThresholdMin.add(new Scalar(60, 60, 60));
                hsvThresholdMax.add(new Scalar(75, 255, 255));
                break;
            case Yellow:
                rgbColour = new Scalar(0, 255, 255);
                hsvThresholdMin.add(new Scalar(17, 60, 60));    // yellow
                hsvThresholdMax.add(new Scalar(38, 255, 255));  // yellow
                break;
            case SpecificYellow:
                rgbColour = new Scalar(0, 255, 255);
                hsvThresholdMin.add(new Scalar(17, 60, 60));
                hsvThresholdMax.add(new Scalar(30, 255, 255));
                break;
        }
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
