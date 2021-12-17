package com.beelder.processor.classbuilder.entities;

public class Variable extends Type {
    /**
     * Type of this variable.
     */
    private final String type;
    /**
     * Value of this variable, might be null.
     */
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
