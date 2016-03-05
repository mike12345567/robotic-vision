package com.queens;

import org.opencv.core.Scalar;

import java.util.ArrayList;

public class Colour {
    public Scalar rgbColour;
    private ArrayList<Scalar> hsvThresholdMin = new ArrayList<Scalar>();
    private ArrayList<Scalar> hsvThresholdMax = new ArrayList<Scalar>();
    int current = 0;
    boolean gotAThreshold = false;

    public Colour(ColourNames colour) {
        switch (colour) {
            case Red:
                rgbColour = new Scalar(0, 0, 255);
                hsvThresholdMin.add(new Scalar(160, 100, 100));
                hsvThresholdMax.add(new Scalar(179, 255, 255));
                hsvThresholdMin.add(new Scalar(0, 100, 100));
                hsvThresholdMax.add(new Scalar(10, 255, 255));
                break;
            case Blue:
                rgbColour = new Scalar(255, 0, 0);
                hsvThresholdMin.add(new Scalar(100, 70, 70));
                hsvThresholdMax.add(new Scalar(110, 255, 255));
                break;
            case Green:
                rgbColour = new Scalar(0, 255, 0);
                hsvThresholdMin.add(new Scalar(30, 60, 60));
                hsvThresholdMax.add(new Scalar(90, 255, 255));
                break;
        }
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
