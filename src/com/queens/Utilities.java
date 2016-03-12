package com.queens;

import java.util.ArrayList;
import java.util.List;

public class Utilities {
    public static List<KeyValueObject> getRotationXYObject(float rotation, int x, int y) {
        ArrayList<KeyValueObject> keyValueObjects = new ArrayList<KeyValueObject>();
        // add the rotation for this object pairing

        keyValueObjects.add(new KeyValueObject("rotation", Float.toString(rotation)));

        // build the location, this a parent JSON object with children of the x and y,
        // server side this is nice to parse, it will appear as location.x/location.y
        ArrayList<KeyValueObject> children = new ArrayList<KeyValueObject>();
        children.add(new KeyValueObject("x", Integer.toString(x)));
        children.add(new KeyValueObject("y", Integer.toString(y)));
        keyValueObjects.add(new KeyValueObject("location", children));
        return keyValueObjects;
    }
}
