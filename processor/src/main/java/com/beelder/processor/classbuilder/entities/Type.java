package com.beelder.processor.classbuilder.entities;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        if(Objects.nonNull(modifier) && !this.modifiers.contains(modifier)) {
            this.modifiers.add(modifier);
        }
    }

    /**
     * Adds a list of new modifiers to this type if they are not yet contained.
     *
     * @param modifiers Modifiers to be added
     */
    public final void addModifiers(final Modifier... modifiers) {
        Arrays.stream(modifiers).filter(m -> !this.modifiers.contains(m)).forEach(this.modifiers::add);
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
