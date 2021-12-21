package com.beelder.processor.classbuilder.entities;

import com.beelder.processor.utils.BeelderUtils;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.beelder.processor.utils.StringBuilderUtils.indent;

public class Clazz extends Type {
    /**
     * Holds all {@link Variable} objects residing in this class.
     */
    private final Set<Variable> variables = new HashSet<>();
    /**
     * Maps method names to {@link Method} objects residing in this class.
     */
    private final Map<String, Method> methods = new HashMap<>();
    /**
     * Contains all constructors of this class as {@link Method} objects.
     */
    private final List<Method> constructors = new ArrayList<>();
    /**
     * This classes package.
     */
    private String packageIdent;

    public Clazz(String key) {
        super(key);
    }

    @Override
    public String build(final int depth) {
        final StringBuilder clazzString = new StringBuilder();
        createPackageLine(clazzString, depth);
        clazzString.append("\n");
        createClazzHeader(clazzString, depth);
        buildCollection(this.variables, clazzString, depth, ";\n");
        clazzString.append("\n");
        buildCollection(this.constructors, clazzString, depth, "\n\n");
        buildCollection(this.methods.values(), clazzString, depth, "\n\n");

        clazzString.append("}");
        return clazzString.toString();
    }

    private void buildCollection(final Collection<? extends Type> col, final StringBuilder sb, final int depth, final String suffix) {
        col.forEach(type -> indent(sb.append(type.build(depth + 1)).append(suffix), depth));
    }

    private void createPackageLine(final StringBuilder sb, final int depth) {
        indent(sb, depth).append("package ").append(this.packageIdent).append(";\n");
    }

    private void createClazzHeader(final StringBuilder sb, final int depth) {
        indent(sb, depth);
        getModifiers().stream().map(BeelderUtils::modififerToLowercase).forEach(m -> sb.append(m).append(" "));

        sb.append("class ").append(getKey()).append(" {\n\n");
    }

    public void addConstructor(final Method method) {
        this.constructors.add(method);
    }

    public boolean containsMethod(final String key) {
        return this.methods.containsKey(key);
    }

    /**
     * Looks up a {@link Method} with the given name in this class, creates
     * it if not existing.
     *
     * @param key The method name
     * @return The method object
     */
    public Method fetchMethod(final String key) {
        return this.methods.computeIfAbsent(key, Method::new);
    }

    /**
     * Adds a new {@link Variable} to this class.
     *
     * @param type The variables type
     * @param key The variables name
     * @param value The variables value, null if not assigned
     * @param modifiers List of all modifiers
     */
    public void addVariable(final String type, final String key, @Nullable final String value, final Modifier... modifiers) {
        if(this.variables.stream().anyMatch(var -> key.equals(var.getKey()))) {
            return;
        }

        final Variable var = new Variable(type, key, value);
        var.addModifiers(modifiers);

        this.variables.add(var);
    }

    /**
     * Adds a new {@link Variable} to this class.
     */
    public void addVariable(final Variable variable) {
        if(this.variables.stream().anyMatch(var -> variable.getKey().equals(var.getKey()))) {
            return;
        }

        this.variables.add(variable);
    }

    /**
     * @return Unmodifiable set with all variables contained in this class
     */
    public Set<Variable> getVariables() {
        return Collections.unmodifiableSet(this.variables);
    }

    public Variable getVariableFor(final String name) {
        return this.variables.stream().filter(var -> name.equals(var.getKey())).findFirst().orElse(null);
    }

    public void setPackageIdent(String packageIdent) {
        this.packageIdent = packageIdent;
    }
}
