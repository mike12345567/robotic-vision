package com.queens;

import java.util.List;

public class KeyValueObject {
    private String key = "";
    private String value = "";
    private List<KeyValueObject> children = null;
    private boolean hasChildren = false;

    public KeyValueObject(String key, String value) {
        this.key = key;
        this.value = value;
        hasChildren = false;
    }

    public KeyValueObject(String key, List<KeyValueObject> children) {
        this.key = key;
        this.children = children;
        hasChildren = true;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public List<KeyValueObject> getChildren() {
        return children;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
