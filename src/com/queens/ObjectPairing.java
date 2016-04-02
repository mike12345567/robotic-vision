package com.queens;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ObjectPairing implements Jsonifable {
    static private final int nearThreshold = 5;
    static private final int queueLength = 10;

    LinkedList<ComparablePoint> latestFrontLocations = new LinkedList<ComparablePoint>();
    LinkedList<ComparablePoint> latestBackLocations = new LinkedList<ComparablePoint>();
    LinkedList<Float> latestRotations = new LinkedList<Float>();

    ColourNames colourOne;  // back area is border colourOne, internal colourTwo
    ColouredArea backArea = null;
    ColourNames colourTwo;  // front area is border colourTwo, internal colourOne
    ColouredArea frontArea = null;
    Point medianFront, medianBack; // save computation cycles, calculated every cycle during rotation calculation

    boolean ready = false;
    long rotateSpeedTimeMs = 0;
    int cyclesSinceTimeUpdate = 0;
    float currentRotationSpeed = 0;

    public ObjectPairing(ColourNames colourOne, ColourNames colourTwo) {
        this.colourOne = colourOne;
        this.colourTwo = colourTwo;
    }

    private <T extends Comparable<T>> T medianList(LinkedList<T> list) {
        if (list.size() == 0) return null;
        LinkedList<T> copiedList = new LinkedList<T>(list);

        Collections.sort(copiedList);

        return copiedList.get(copiedList.size()/2);
    }

    public int getX() {
        if (ready && medianFront != null && medianBack != null) {
            Point front = medianFront;
            Point back = medianBack;
            return (int)(back.x + front.x / 2);
        } else {
            return 0;
        }
    }

    public int getY() {
        if (ready && medianFront != null && medianBack != null) {

            Point front = medianFront;
            Point back = medianBack;
            return (int)(back.y + front.y / 2);
        } else {
            return 0;
        }
    }

    public float getRotation() {
        if (ready) {
            Float medianRotation = medianList(latestRotations);
            if (medianRotation == null) {
                medianRotation = 0f;
            }
            return medianRotation;
        } else {
            return 0f;
        }
    }

    private float calculateRotation(Point front, Point back) {
        if (ready && front != null && back != null) {
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
            int startRotation = (int) calculateRotation(latestFrontLocations.getLast(), latestBackLocations.getLast());
            int endRotation = (int) calculateRotation(latestFrontLocations.getFirst(), latestBackLocations.getFirst());

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

            ColouredArea previousFront = frontArea, previousBack = backArea;
            if (area.getColour() == this.colourOne && containsOther(currentAreas, area) && !tooClose(area, backArea)) {
                this.frontArea = area;
            }
            if (area.getColour() == this.colourTwo && containsOther(currentAreas, area) && !tooClose(area, frontArea)) {
                this.backArea = area;
            }
            if (tooClose(this.frontArea, this.backArea)) {
                this.frontArea = previousFront;
                this.backArea = previousBack;
            }
        }

        if (this.frontArea != null && this.backArea != null) {
            medianFront = medianList(latestFrontLocations);
            medianBack = medianList(latestBackLocations);
            ready = true;
            latestFrontLocations.offer(new ComparablePoint(frontArea.getPureX(), frontArea.getPureY()));
            if (latestFrontLocations.size() > queueLength) {
                latestFrontLocations.pop();
            }
            latestBackLocations.offer(new ComparablePoint(backArea.getPureX(), backArea.getPureY()));
            if (latestBackLocations.size() > queueLength) {
                latestBackLocations.pop();
            }
            latestRotations.offer(calculateRotation(medianFront, medianBack));
            if (latestRotations.size() > queueLength) {
                latestRotations.pop();
            }
            cyclesSinceTimeUpdate++;
            if (cyclesSinceTimeUpdate > queueLength) {
                currentRotationSpeed = getRotationSpeed();
                rotateSpeedTimeMs = System.currentTimeMillis();
                cyclesSinceTimeUpdate = 0;
            }
        } else {
            ready = false;
        }
    }

    private boolean tooClose(ColouredArea areaOne, ColouredArea areaTwo) {
        if (areaOne != null && areaTwo != null && areaOne.close(areaTwo.getBoundingBox(), 5)) {
            return true;
        }
        return false;
    }

    private boolean containsOther(ArrayList<ColouredArea> currentAreas, ColouredArea toTest) {
        ColourNames lookingFor = toTest.getColour() == colourOne ? colourTwo : colourOne;
        for (ColouredArea area : currentAreas) {
            if (area == toTest || tooClose(area, toTest)) {
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
