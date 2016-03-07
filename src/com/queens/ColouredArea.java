package com.queens;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public class ColouredArea {
    private static final int threshold = 30;
    private Rect boundingBox = null;
    private ColourNames colour;
    private boolean needsUpdated = false;

    public ColouredArea(Rect boundingBox, ColourNames colour) {
        this.colour = colour;
        this.boundingBox = boundingBox;
    }

    public int getX() {
        return this.boundingBox.x + this.boundingBox.width/2;
    }

    public int getY() {
        return this.boundingBox.y + this.boundingBox.height/2;
    }

    public boolean withinArea(Rect newPosition) {
        return withinThreshold(boundingBox.tl(), newPosition.tl()) &&
               withinThreshold(boundingBox.br(), newPosition.br());
    }

    private boolean withinThreshold(Point currentPos, Point newPos) {
        return (currentPos.x > (newPos.x - threshold) && currentPos.x < (newPos.x + threshold)) &&
               (currentPos.y > (newPos.y - threshold) && currentPos.y < (newPos.y + threshold));
    }

    public ColourNames getColour() {
        return colour;
    }

    public boolean hasBeenUpdated() {
        return !needsUpdated;
    }

    public void resetUpdated() {
        needsUpdated = true;
    }

    public void updatePosition(Rect newPosition) {
        this.boundingBox = newPosition;
        needsUpdated = false;
    }
}
