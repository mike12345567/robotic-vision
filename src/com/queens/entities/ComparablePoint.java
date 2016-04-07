package com.queens.entities;

import com.queens.utilities.Utilities;
import org.opencv.core.Point;

public class ComparablePoint extends Point implements Comparable<ComparablePoint> {
    public ComparablePoint(int x, int y) {
        super(x, y);
    }

    @Override
    public int compareTo(ComparablePoint o) {
        int difference = Utilities.calculatePointDifference(this, o, false);

        if (difference > 0) return 1;
        if (difference < 0) return -1;
        return 0;
    }
}
