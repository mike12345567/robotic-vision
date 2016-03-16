package com.queens.testing;

import com.queens.Jsonifable;
import com.queens.KeyValueObject;
import com.queens.Utilities;

import java.util.List;

public class TestPairing implements Jsonifable {
    private float rotation = 0.0f;
    private int xLocation = 0;
    private int yLocation = 0;

    public void update(float rotation, int xLocation, int yLocation) {
        this.rotation = rotation;
        this.xLocation = xLocation;
        this.yLocation = yLocation;
    }

    @Override
    public List<KeyValueObject> getKeyValuePairs() {
        return Utilities.getRotationXYObject(rotation, xLocation, yLocation);
    }
}