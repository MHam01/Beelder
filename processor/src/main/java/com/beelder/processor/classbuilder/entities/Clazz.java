package com.beelder.processor.classbuilder.entities;

public class Clazz extends Type {
    private String packageIdent;

    public Clazz(String key) {
        super(key);
    }

    public void setPackageIdent(String packageIdent) {
        this.packageIdent = packageIdent;
    }
}
