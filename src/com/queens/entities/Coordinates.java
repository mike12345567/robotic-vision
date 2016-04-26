package com.queens.entities;

import com.queens.communications.IJsonifable;
import com.queens.communications.KeyValueObject;

import java.util.ArrayList;
import java.util.List;

public class Coordinates implements IJsonifable {
    private int x, y;

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public List<KeyValueObject> getKeyValuePairs() {
        ArrayList<KeyValueObject> keyValueObjects = new ArrayList<KeyValueObject>();

        keyValueObjects.add(new KeyValueObject("x", Integer.toString(x)));
        keyValueObjects.add(new KeyValueObject("y", Integer.toString(y)));
        return keyValueObjects;
    }
}
