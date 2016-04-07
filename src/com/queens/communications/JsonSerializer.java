package com.queens.communications;

import com.sun.media.sound.InvalidDataException;

import javax.json.*;
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

    public void addSection(String key, Jsonifable object) {
        List<KeyValueObject> keyValuePairs = object.getKeyValuePairs();
        if (keyValuePairs == null) {
            return;
        }

        if (builder == null) {
            builder = factory.createObjectBuilder();
        }
        // TODO: deal with multiple robots in the scene
        KeyValueObject externalObject = new KeyValueObject(key, keyValuePairs);
        convertKeyValuePair(builder, externalObject);
    }

    public String finish() throws InvalidDataException {
        if (!ready) {
            throw new InvalidDataException("No JSON data provided");
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
