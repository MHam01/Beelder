package com.beelder.processor.classbuilder.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Method extends Type {
    private Set<Variable> parameters = new HashSet<>();
    private final List<String> content = new ArrayList<>();

    private String returnType = "void";

    public Method(String key) {
        super(key);
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public void addLine(final String line) {
        this.content.add(line);
    }

    public void addParameter(final Variable variable) {
        this.parameters.add(variable);
    }

    public int parameterNum() {
        return this.parameters.size();
    }
}
