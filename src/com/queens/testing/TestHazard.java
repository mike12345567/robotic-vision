package com.queens.testing;

import com.queens.communications.IJsonifable;
import com.queens.communications.KeyValueObject;
import com.queens.utilities.Utilities;

import java.util.List;

public class TestHazard implements IJsonifable {
    int xLocation, yLocation, width, height;

    public void update(int xLocation, int yLocation, int width, int height) {
        this.xLocation = xLocation;
        this.yLocation = yLocation;
        this.width = width;
        this.height = height;
    }

    @Override
    public List<KeyValueObject> getKeyValuePairs() {
        return Utilities.getRectObject(xLocation, yLocation, width, height);
    }
}
