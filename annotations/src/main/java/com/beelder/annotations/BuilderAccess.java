package com.beelder.annotations;

import javax.lang.model.element.Modifier;

public enum BuilderAccess {
    PACKAGE_PRIVATE(null), PUBLIC(Modifier.PUBLIC);

    private final Modifier modifier;

    BuilderAccess(final Modifier modifier) {
        this.modifier = modifier;
    }

    public Modifier getModifier() {
        return this.modifier;
    }
}
