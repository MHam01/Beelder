package com.beelder.processor.classbuilder.entities;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Clazz extends Type {
    private final Set<Variable> variables = new HashSet<>();
    private String packageIdent;

    public Clazz(String key) {
        super(key);
    }

    public void setPackageIdent(String packageIdent) {
        this.packageIdent = packageIdent;
    }

    public void addVariable(final String type, final String key, final String value, final Modifier... modifiers) {
        final Variable var = new Variable(type, key, value);
        Arrays.stream(modifiers).forEach(var::addModifier);

        this.variables.add(var);
    }
}
