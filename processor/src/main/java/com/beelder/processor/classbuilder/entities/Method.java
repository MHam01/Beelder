package com.beelder.processor.classbuilder.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Method extends Type {
    /**
     * All {@link Variable} objects resembling this methods parameters.
     */
    private Set<Variable> parameters = new HashSet<>();
    /**
     * This methods body as a list of lines.
     */
    private final List<String> content = new ArrayList<>();

    /**
     * The return type of this method.
     */
    private String returnType = "void";



    public Method(String key) {
        super(key);
    }


    /**
     * Adds a new line to the body of this method.
     *
     * @param line The line
     */
    public void addLine(final String line) {
        this.content.add(line);
    }

    /**
     * Adds a new variable to the parameters list.
     *
     * @param variable The variable object
     */
    public void addParameter(final Variable variable) {
        this.parameters.add(variable);
    }

    /**
     * @return The current number of parameters
     */
    public int parameterNum() {
        return this.parameters.size();
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
