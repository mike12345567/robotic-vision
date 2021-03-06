package com.queens.entities;

import com.queens.utilities.Utilities;
import com.queens.colours.ColourNames;
import org.opencv.core.Point;
import org.opencv.core.Rect;

public class ColouredArea {
    private static final int maxFramesSinceUseForDraw = 10;
    private static final int locationThreshold = 30;
    private static final int sizeThreshold = 5;
    private static final float percentageThreshold = 0.4f;
    private Rect boundingBox = null;
    private ColourNames colour;
    private boolean needsUpdated = false;
    private boolean inUse = false;
    private int framesSinceInUse = maxFramesSinceUseForDraw; // start as not being drawn

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
        return Utilities.calculatePointDifference(position.tl(), this.boundingBox.tl(), true) < threshold * 2;
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

    public boolean shouldDraw() {
        if (!isInUse()) {
            framesSinceInUse++;
        } else {
            framesSinceInUse--;
        }
        return framesSinceInUse < maxFramesSinceUseForDraw;
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

    public void setInUse(boolean inUse) {
        if (!inUse) framesSinceInUse = maxFramesSinceUseForDraw;
        this.inUse = inUse;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void updatePosition(Rect newPosition) {
        this.boundingBox = newPosition;
        needsUpdated = false;
    }
}
