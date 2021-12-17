package com.beelder.processor.classbuilder.entities;

public class Variable extends Type {
    private final String type;
    private final String value;

    public Variable(String type, String key) {
        super(key);

        this.type = type;
        this.value = null;
    }

    public Variable(String type, String key, String value) {
        super(key);

        this.type = type;
        this.value = value;
    }

    public String getType() {
        return this.type;
    }
}
