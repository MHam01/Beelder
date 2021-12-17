package com.beelder.processor.classbuilder.entities;

import com.beelder.processor.utils.BeelderUtils;

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
        variables.forEach(var -> indent(clazzString.append(var.build(depth + 1)).append(";\n"), depth));
        clazzString.append("\n\n");
        methods.values().forEach(method -> indent(clazzString.append(method.build(depth + 1)).append("\n\n"), depth));

        clazzString.append("}");
        return clazzString.toString();
    }

    private void createPackageLine(final StringBuilder sb, final int depth) {
        indent(sb, depth).append("package ").append(this.packageIdent).append(";\n");
    }

    private void createClazzHeader(final StringBuilder sb, final int depth) {
        indent(sb, depth);
        getModifiers().stream().map(BeelderUtils::modififerToLowercase).forEach(m -> sb.append(m).append(" "));

        sb.append("class ").append(getKey()).append(" {\n\n");
    }

    /**
     * Looks up a {@link Method} with the given name in this class, creates
     * it if not existing.
     *
     * @param key The method name
     * @return The method object
     */
    public Method fetchMethod(final String key) {
        return methods.computeIfAbsent(key, Method::new);
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
        final Variable var = new Variable(type, key, value);
        Arrays.stream(modifiers).forEach(var::addModifier);

        this.variables.add(var);
    }

    /**
     * @return Unmodifiable set with all variables contained in this class
     */
    public Set<Variable> getVariables() {
        return Collections.unmodifiableSet(this.variables);
    }

    public void setPackageIdent(String packageIdent) {
        this.packageIdent = packageIdent;
    }
}
