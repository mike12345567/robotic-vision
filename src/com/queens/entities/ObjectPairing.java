package com.queens.entities;

import com.queens.utilities.Utilities;
import com.queens.colours.ColourNames;
import com.queens.communications.Jsonifable;
import com.queens.communications.KeyValueObject;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ObjectPairing implements Jsonifable {
    static private final int queueLength = 3;
    static private final int msToSeconds = 1000;

    LinkedList<ComparablePoint> latestFrontLocations = new LinkedList<ComparablePoint>();
    LinkedList<ComparablePoint> latestBackLocations = new LinkedList<ComparablePoint>();
    LinkedList<Float> latestRotations = new LinkedList<Float>();
    
    private String pairingName;
    ColourNames colourOne;  // back area is border colourOne, internal colourTwo
    BorderedArea backArea = null;
    ColourNames colourTwo;  // front area is border colourTwo, internal colourOne
    BorderedArea frontArea = null;
    Point medianFront, medianBack; // save computation cycles, calculated every cycle during rotation calculation

    boolean ready = false;
    long rotateSpeedTimeMs = 0;
    int cyclesSinceTimeUpdate = 0;
    float currentRotationSpeed = 0;

    public ObjectPairing(String pairingName, ColourNames colourOne, ColourNames colourTwo) {
        this.colourOne = colourOne;
        this.colourTwo = colourTwo;
        this.pairingName = pairingName;

        frontArea = new BorderedArea(true, colourTwo, colourOne);
        backArea = new BorderedArea(true, colourOne, colourTwo);
    }

    private <T extends Comparable<T>> T medianList(LinkedList<T> list) {
        if (list.size() == 0) return null;
        LinkedList<T> copiedList = new LinkedList<T>(list);

        Collections.sort(copiedList);

        return copiedList.get(copiedList.size()/2);
    }

    public int getX() {
        if (ready && medianFront != null && medianBack != null) {
            double smaller = Math.min(medianFront.x, medianBack.x);
            double larger = Math.max(medianFront.x, medianBack.x);
            int difference = (int)(larger - smaller);
            return (int)(smaller + difference);
        } else {
            return 0;
        }
    }

    public int getY() {
        if (ready && medianFront != null && medianBack != null) {
            double smaller = Math.min(medianFront.y, medianBack.y);
            double larger = Math.max(medianFront.y, medianBack.y);
            int difference = (int)(larger - smaller);
            return (int)(smaller + difference);
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

    public boolean isActive() {
        return ready;
    }

    public String getPairingName() {
        return pairingName;
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
            float output = ((float)distance / timeDiff);
            output *= msToSeconds;
            if (output < 1) output = 0f;
            return output;
        }
        return 0;
    }

    public void checkForPairing(ArrayList<ColouredArea> currentAreas) {
        for (ColouredArea area : currentAreas) {
            if (!area.isRoughSquare() || area.isInUse()) {
                continue;
            }

            BorderedArea previousFront = frontArea, previousBack = backArea;
            frontArea.update(currentAreas);
            backArea.update(currentAreas);
            if (Utilities.tooClose(this.frontArea.getArea(), this.backArea.getArea())) {
                this.frontArea = previousFront;
                this.backArea = previousBack;
            }
        }

        updateQueues();
    }

    private void updateQueues() {
        ColouredArea frontColouredArea = this.frontArea.getArea();
        ColouredArea backColouredArea = this.backArea.getArea();

        if (frontColouredArea != null && backColouredArea != null) {
            medianFront = medianList(latestFrontLocations);
            medianBack = medianList(latestBackLocations);
            ready = true;
            latestFrontLocations.offer(new ComparablePoint(frontColouredArea.getPureX(), frontColouredArea.getPureY()));
            if (latestFrontLocations.size() > queueLength) {
                latestFrontLocations.pop();
            }
            latestBackLocations.offer(new ComparablePoint(backColouredArea.getPureX(), backColouredArea.getPureY()));
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

    @Override
    public List<KeyValueObject> getKeyValuePairs() {
        if (!ready) {
            return null;
        }

        return Utilities.getRotationXYObject(getRotation(), getX(), getY(), currentRotationSpeed);
    }
}
