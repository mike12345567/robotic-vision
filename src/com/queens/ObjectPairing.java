package com.queens;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class ObjectPairing implements Jsonifable {
    static private final int nearThreshold = 5;

    ColourNames colourOne;  // back area is border colourOne, internal colourTwo
    ColouredArea backArea = null;
    ColourNames colourTwo;  // front area is border colourTwo, internal colourOne
    ColouredArea frontArea = null;
    boolean ready = false;

    public ObjectPairing(ColourNames colourOne, ColourNames colourTwo) {
        this.colourOne = colourOne;
        this.colourTwo = colourTwo;
    }

    public int getX() {
        if (ready) {
            return backArea.getX() + frontArea.getX() / 2;
        } else {
            return 0;
        }
    }

    public int getY() {
        if (ready) {
            return backArea.getY() + frontArea.getY() / 2;
        } else {
            return 0;
        }
    }

    public float getRotation() {
        if (ready) {
            double x = backArea.getX() - frontArea.getX();
            double y = backArea.getY() - frontArea.getY();
            double length = Math.sqrt(x * x + y * y);
            if (length != 0) {
                x = x / length;
                y = y / length;
            }

            double degrees = 90.0d - Math.toDegrees( Math.atan2( y, x ) );

            if( degrees < 0.0d )
            {
                degrees += 360.0;
            }
            return (float) degrees;
        } else {
            return 0f;
        }
    }

    public void checkForPairing(ArrayList<ColouredArea> currentAreas) {
        if (!currentAreas.contains(backArea)) {
            backArea = null;
        }
        if (!currentAreas.contains(frontArea)) {
            frontArea = null;
        }

        for (ColouredArea area : currentAreas) {

            if (!area.isRoughSquare()) {
                continue;
            }

            if (area.getColour() == this.colourOne && containsOther(currentAreas, area)) {
                this.frontArea = area;
            }
            if (area.getColour() == this.colourTwo && containsOther(currentAreas, area)) {
                this.backArea = area;
            }
        }

        if (this.frontArea != null && this.backArea != null) {
            ready = true;
        } else {
            ready = false;
        }
    }

    private boolean containsOther(ArrayList<ColouredArea> currentAreas, ColouredArea toTest) {
        ColourNames lookingFor = toTest.getColour() == colourOne ? colourTwo : colourOne;
        for (ColouredArea area : currentAreas) {
            if (area == toTest || (pointsClose(area.getTopCorner(), toTest.getTopCorner()) &&
                pointsClose(area.getBottomCorner(), toTest.getBottomCorner()))) {
                continue;
            }

            if (area.getColour() == lookingFor &&
                toTest.getBoundingBox().contains(area.getTopCorner()) &&
                toTest.getBoundingBox().contains(area.getBottomCorner())) {
                return true;
            }
        }
        return false;
    }

    private boolean pointsClose(Point one, Point two) {
        return (one.x - nearThreshold > two.x && one.x + nearThreshold < two.x &&
                one.y - nearThreshold > two.y && one.y + nearThreshold < two.y);
    }

    @Override
    public List<KeyValueObject> getKeyValuePairs() {
        if (!ready) {
            return null;
        }

        ArrayList<KeyValueObject> keyValueObjects = new ArrayList<KeyValueObject>();
        // add the rotation for this object pairing

        keyValueObjects.add(new KeyValueObject("rotation", Float.toString(getRotation())));

        // build the location, this a parent JSON object with children of the x and y,
        // server side this is nice to parse, it will appear as location.x/location.y
        ArrayList<KeyValueObject> children = new ArrayList<KeyValueObject>();
        children.add(new KeyValueObject("x", Integer.toString(getX())));
        children.add(new KeyValueObject("y", Integer.toString(getY())));
        keyValueObjects.add(new KeyValueObject("location", children));
        return keyValueObjects;
    }
}
