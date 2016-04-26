package com.queens.communications;

import com.queens.utilities.Utilities;

import javax.json.*;
import java.util.ArrayList;
import java.util.List;

public class JsonSerializer {
    JsonBuilderFactory factory;
    boolean ready = false;
    JsonObjectBuilder builder;

    public JsonSerializer() {
        factory = Json.createBuilderFactory(null);
    }

    public void start() {
        ready = false;
        builder = null;
    }

    public <T extends IJsonifable> void addArray(String arrayKey, String baseObjKey, ArrayList<T> objects) {
        JsonArrayBuilder arrayBuilder = factory.createArrayBuilder();
        int count = 1;
        for (IJsonifable object : objects) {
            JsonObjectBuilder objBuilder = factory.createObjectBuilder();
            String key = Utilities.generateArrayElemName(baseObjKey, count++);
            convertKeyValuePair(objBuilder, new KeyValueObject(key, object.getKeyValuePairs()));
            arrayBuilder.add(objBuilder.build());
        }
        if (builder == null) {
            builder = factory.createObjectBuilder();
        }
        builder.add(arrayKey, arrayBuilder.build());
    }

    public void addSection(String key, IJsonifable object) {
        List<KeyValueObject> keyValuePairs = object.getKeyValuePairs();
        if (keyValuePairs == null) {
            return;
        }

        if (builder == null) {
            builder = factory.createObjectBuilder();
        }

        KeyValueObject externalObject = new KeyValueObject(key, keyValuePairs);
        convertKeyValuePair(builder, externalObject);
    }

    public void addSingleData(String key, String value) {
        if (builder == null) {
            builder = factory.createObjectBuilder();
        }

        builder.add(key, value);
    }

    public String finish() {
        if (!ready) {
            return null;
        }

        return builder.build().toString();
    }

    private void convertKeyValuePair(JsonObjectBuilder builder, KeyValueObject keyValue) {
        if (keyValue.hasChildren()) {
            List<KeyValueObject> children = keyValue.getChildren();
            JsonObjectBuilder objectBuilder = factory.createObjectBuilder();
            for (int i = 0; i < children.size(); i++) {
                convertKeyValuePair(objectBuilder, children.get(i));
            }
            builder.add(keyValue.getKey(), objectBuilder.build());
        } else {
            // single element of data with a value
            builder.add(keyValue.getKey(), keyValue.getValue());
        }
        ready = true;
    }
}
