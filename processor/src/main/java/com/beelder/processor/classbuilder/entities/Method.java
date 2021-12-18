package com.beelder.processor.classbuilder.entities;

import com.beelder.processor.utils.BeelderUtils;
import com.beelder.processor.utils.StringBuilderUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Method extends Type {
    /**
     * All {@link Variable} objects resembling this methods parameters.
     */
    private List<Variable> parameters = new ArrayList<>();
    /**
     * This methods body as a list of lines.
     */
    private final List<String> content = new ArrayList<>();

    /**
     * The return type of this method.
     */
    private String returnType = "void";

    /**
     * If set to true, this methods body cannot be modified anymore.
     */
    private boolean locked = false;



    public Method(String key) {
        super(key);
    }


    @Override
    public String build(final int depth) {
        final StringBuilder methodString = new StringBuilder();
        createMethodHeader(methodString, depth);
        createMethodBody(methodString, depth + 1);
        return methodString.toString();
    }

    /**
     * Creates the header for this method in the form "modifier0 modifier1 ... returnType key(Variable0, Variable1, ...)"
     */
    private void createMethodHeader(final StringBuilder sb, final int depth) {
        StringBuilderUtils.indent(sb, depth);
        getModifiers().stream().map(BeelderUtils::modififerToLowercase).forEach(m -> sb.append(m).append(" "));

        sb.append(this.returnType).append(" ").append(getKey()).append("(").append(parameters.stream().map(Variable::build).collect(Collectors.joining(", "))).append(") {\n");
    }

    /**
     * Creates the body for this method.
     */
    private void createMethodBody(final StringBuilder sb, final int depth) {
        content.forEach(l -> StringBuilderUtils.indent(sb, depth).append(l).append("\n"));
        StringBuilderUtils.indent(sb, depth - 1).append('}');
    }

    /**
     * Adds a new line to the body of this method.
     *
     * @param line The line
     */
    public void addLine(final String line) {
        if(locked) {
            return;
        }

        this.content.add(line);
    }

    /**
     * Adds a return statement to this method, locks this methods body from further modification.
     *
     * @param statement The statement (without "return" and ";")
     */
    public void addReturnStatement(@Nullable final String statement) {
        addLine("return".concat(StringUtils.isBlank(statement) ? ";" : " ".concat(statement).concat(";")));
        this.locked = true;
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

    /**
     * @return Unmodifiable list with all parameter variables
     */
    public List<Variable> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
