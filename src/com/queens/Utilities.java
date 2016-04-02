package com.queens;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Utilities {
    public static List<KeyValueObject> getRotationXYObject(float rotation, int x, int y, float rotationSpeed) {
        ArrayList<KeyValueObject> keyValueObjects = new ArrayList<KeyValueObject>();
        // add the rotation for this object pairing

        keyValueObjects.add(new KeyValueObject("rotation", Float.toString(rotation)));
        keyValueObjects.add(new KeyValueObject("rotationSpeed", Float.toString(rotationSpeed)));

        // build the location, this a parent JSON object with children of the x and y,
        // server side this is nice to parse, it will appear as location.x/location.y
        ArrayList<KeyValueObject> children = new ArrayList<KeyValueObject>();
        children.add(new KeyValueObject("x", Integer.toString(x)));
        children.add(new KeyValueObject("y", Integer.toString(y)));
        keyValueObjects.add(new KeyValueObject("location", children));
        return keyValueObjects;
    }

    public static int calculateRectDifference(Rect p1, Rect p2) {
        int tlx = (int)(p1.tl().x - p2.tl().x);
        if (tlx < 0) tlx *= -1;
        int tly = (int)(p1.tl().y - p2.tl().y);
        if (tly < 0) tly *= -1;
        int brx = (int)(p1.br().x - p2.br().x);
        if (brx < 0) brx *= -1;
        int bry = (int)(p1.br().y - p2.br().y);
        if (bry < 0) bry *= -1;
        return tlx + tly + brx + bry;
    }

    public static int calculatePointDifference(Point p1, Point p2, boolean normalise) {
        int x = (int)(p1.x - p2.x);
        if (normalise && x < 0) x *= -1;
        int y = (int)(p1.y - p2.y);
        if (normalise && y < 0) y *= -1;
        return x + y;
    }

    public static Rect contourToRect(MatOfPoint contour) {
        MatOfPoint2f point2f = new MatOfPoint2f();
        Imgproc.approxPolyDP(convertTo2f(contour), point2f, 3, true);
        Rect rect = Imgproc.boundingRect(convertToP(point2f));
        return rect;
    }

    public static MatOfPoint2f convertTo2f(MatOfPoint point) {
        MatOfPoint2f point2f = new MatOfPoint2f();
        point2f.fromList(point.toList());
        return point2f;
    }

    public static MatOfPoint convertToP(MatOfPoint2f point2f) {
        MatOfPoint point = new MatOfPoint();
        point.fromList(point2f.toList());
        return point;
    }
}
