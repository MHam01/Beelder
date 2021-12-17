package com.beelder.processor.classbuilder.entities;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Type {
    private final List<Modifier> modifiers = new ArrayList<>();

    private String key;

    public Type(final String key) {
        this.key = key;
    }

    public final String getKey() {
        return this.key;
    }

    public final List<Modifier> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    public final void addModifier(final Modifier modifier) {
        if(!this.modifiers.contains(modifier)) {
            this.modifiers.add(modifier);
        }
    }
}
