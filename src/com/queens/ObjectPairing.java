package com.queens;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ObjectPairing implements Jsonifable {
    static private final int nearThreshold = 5;
    static private final int speedCalculation = 10;
    static private final int avgCalculation = 3;

    LinkedList<Point> latestFrontLocations = new LinkedList<Point>();
    LinkedList<Point> latestBackLocations = new LinkedList<Point>();

    ColourNames colourOne;  // back area is border colourOne, internal colourTwo
    ColouredArea backArea = null;
    ColourNames colourTwo;  // front area is border colourTwo, internal colourOne
    ColouredArea frontArea = null;

    boolean ready = false;
    long rotateSpeedTimeMs = 0;
    int cyclesSinceTimeUpdate = 0;
    float currentRotationSpeed = 0;

    public ObjectPairing(ColourNames colourOne, ColourNames colourTwo) {
        this.colourOne = colourOne;
        this.colourTwo = colourTwo;
    }

    private Point avgLocation(LinkedList<Point> points, int count) {
        int avgX = 0, avgY = 0;

        if (count > points.size()) count = points.size();
        for (int i = 0; i < count; i++) {
            avgX += points.get(i).x;
            avgY += points.get(i).y;
        }

        return new Point(avgX / count, avgY / count);
    }

    public int getX() {
        if (ready) {
            Point front = avgLocation(latestFrontLocations, avgCalculation);
            Point back = avgLocation(latestBackLocations, avgCalculation);
            return (int)(back.x + front.x / 2);
        } else {
            return 0;
        }
    }

    public int getY() {
        if (ready) {
            Point front = avgLocation(latestFrontLocations, avgCalculation);
            Point back = avgLocation(latestBackLocations, avgCalculation);
            return (int)(back.y + front.y / 2);
        } else {
            return 0;
        }
    }

    public float getRotation() {
        return getRotation(avgLocation(latestFrontLocations, avgCalculation),
                           avgLocation(latestBackLocations, avgCalculation));
    }

    private float getRotation(Point front, Point back) {
        if (ready) {
            double x = back.x - front.x;
            double y = back.y - front.y;
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

    public float getRotationSpeed() {
        if (ready && rotateSpeedTimeMs != 0) {
            int startRotation = (int) getRotation(latestFrontLocations.getLast(), latestBackLocations.getLast());
            int endRotation = (int) getRotation(latestFrontLocations.getFirst(), latestBackLocations.getFirst());

            int phi = Math.abs(endRotation - startRotation) % 360;       // This is either the distance or 360 - distance
            int distance = phi > 180 ? 360 - phi : phi;
            int timeDiff = (int)(System.currentTimeMillis() - rotateSpeedTimeMs);
            return ((float)distance / timeDiff);
        }
        return 0;
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

            if (area.getColour() == this.colourOne && containsOther(currentAreas, area) && testDistance(area, backArea)) {
                this.frontArea = area;
            }
            if (area.getColour() == this.colourTwo && containsOther(currentAreas, area) && testDistance(area, frontArea)) {
                this.backArea = area;
            }
        }

        if (this.frontArea != null && this.backArea != null) {
            ready = true;
            latestFrontLocations.offer(new Point(frontArea.getX(), frontArea.getY()));
            if (latestFrontLocations.size() > speedCalculation) {
                latestFrontLocations.pop();
            }
            latestBackLocations.offer(new Point(backArea.getX(), backArea.getY()));
            if (latestBackLocations.size() > speedCalculation) {
                latestBackLocations.pop();
            }
            cyclesSinceTimeUpdate++;
            if (cyclesSinceTimeUpdate > speedCalculation) {
                currentRotationSpeed = getRotationSpeed();
                rotateSpeedTimeMs = System.currentTimeMillis();
                cyclesSinceTimeUpdate = 0;
            }
        } else {
            ready = false;
        }
    }

    private boolean testDistance(ColouredArea areaOne, ColouredArea areaTwo) {
        if (areaOne == null || areaTwo == null) {
            return true;
        }
        return true;
    }

    private boolean containsOther(ArrayList<ColouredArea> currentAreas, ColouredArea toTest) {
        ColourNames lookingFor = toTest.getColour() == colourOne ? colourTwo : colourOne;
        for (ColouredArea area : currentAreas) {
            if (area == toTest) {
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

    private boolean pointsClose(Point one, Point two, int threshold) {
        return (one.x - threshold < two.x && one.x + threshold > two.x &&
                one.y - threshold < two.y && one.y + threshold > two.y);
    }

    @Override
    public List<KeyValueObject> getKeyValuePairs() {
        if (!ready) {
            return null;
        }

        return Utilities.getRotationXYObject(getRotation(), getX(), getY(), currentRotationSpeed);
    }
}
