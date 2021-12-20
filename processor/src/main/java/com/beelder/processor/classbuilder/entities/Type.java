package com.beelder.processor.classbuilder.entities;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Type {
    /**
     * All {@link Modifier}s of this type.
     */
    private final List<Modifier> modifiers = new ArrayList<>();

    private String key;

    /**
     * Creates a new type by the given name.
     *
     * @param key The type name
     */
    public Type(final String key) {
        this.key = key;
    }


    public final void setKey(String key) {
        this.key = key;
    }

    public final String getKey() {
        return this.key;
    }

    public final List<Modifier> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    /**
     * Adds a new modifier to this type if it is not yet contained.
     *
     * @param modifier Modifier to be added
     */
    public final void addModifier(final Modifier modifier) {
        if(!this.modifiers.contains(modifier)) {
            this.modifiers.add(modifier);
        }
    }

    /**
     * @return Complex toString representation of this type
     */
    public final String build() {
        return build(0);
    }

    /**
     * @param depth The current depth
     * @return Complex toString representation of this type
     */
    public abstract String build(final int depth);
}
