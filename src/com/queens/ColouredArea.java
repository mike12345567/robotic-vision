package com.queens;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public class ColouredArea {
    private static final int locationThreshold = 30;
    private static final int sizeThreshold = 5;
    private static final float percentageThreshold = 0.4f;
    private Rect boundingBox = null;
    private ColourNames colour;
    private boolean needsUpdated = false;

    public ColouredArea(Rect boundingBox, ColourNames colour) {
        this.colour = colour;
        this.boundingBox = boundingBox;
    }

    public Point getTopCorner() {
        return boundingBox.tl();
    }

    public Point getBottomCorner() {
        return boundingBox.br();
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public int getPureX() {
        return boundingBox.x;
    }

    public int getPureY() {
        return boundingBox.y;
    }

    public int getX() {
        return this.boundingBox.x + this.boundingBox.width/2;
    }

    public int getY() {
        return this.boundingBox.y + this.boundingBox.height/2;
    }

    public boolean withinArea(Rect newPosition) {
        return withinArea(newPosition, locationThreshold);
    }

    public boolean withinArea(Rect position, int threshold) {
        return withinThreshold(boundingBox.tl(), position.tl(), threshold) &&
                withinThreshold(boundingBox.br(), position.br(), threshold);
    }

    private boolean withinThreshold(Point currentPos, Point newPos, int threshold) {
        return (currentPos.x > (newPos.x - threshold) && currentPos.x < (newPos.x + threshold)) &&
               (currentPos.y > (newPos.y - threshold) && currentPos.y < (newPos.y + threshold));
    }

    public boolean close(Rect position, int threshold) {
        return Utilities.calculateRectDifference(position, this.boundingBox) < threshold * 4;
    }

    public boolean withinSize(Rect rect) {
        int diffWidth = this.boundingBox.width - rect.width;
        if (diffWidth < 0) diffWidth *= -1;
        int diffHeight = this.boundingBox.height - rect.height;
        if (diffHeight < 0) diffHeight *= -1;
        return (diffWidth < sizeThreshold && diffHeight < sizeThreshold);
    }

    public ColourNames getColour() {
        return colour;
    }

    public int getSize() {
        return this.boundingBox.width * this.boundingBox.height;
    }

    public int getWidth() {
        return this.boundingBox.width;
    }

    public int getHeight() {
        return this.boundingBox.height;
    }

    public boolean isRoughSquare() {
        float pixelsPercent = boundingBox.width * percentageThreshold;
        return (boundingBox.width + pixelsPercent > boundingBox.height &&
                boundingBox.width - pixelsPercent < boundingBox.height);
    }

    public boolean hasBeenUpdated() {
        return !needsUpdated;
    }

    public void resetUpdated() {
        needsUpdated = true;
    }

    public void merge(Rect rect) {
        double tlx, tly, brx, bry, difference;
        tlx = boundingBox.tl().x > rect.tl().x ? rect.tl().x : boundingBox.tl().x;
        tly = boundingBox.tl().y > rect.tl().y ? rect.tl().y : boundingBox.tl().y;
        brx = boundingBox.br().x < rect.br().x ? rect.br().x : boundingBox.br().x;
        bry = boundingBox.br().y < rect.br().y ? rect.br().y : boundingBox.br().y;

        double[] vals = new double[4];
        vals[0] = tlx;
        vals[1] = tly;
        difference = tlx - brx;
        if (difference < 0) difference *= -1;
        vals[2] = difference;
        difference = tly - bry;
        if (difference < 0) difference *= -1;
        vals[3] = difference;
        boundingBox.set(vals);
    }

    public void updatePosition(Rect newPosition) {
        this.boundingBox = newPosition;
        needsUpdated = false;
    }
}
