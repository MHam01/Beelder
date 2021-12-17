package com.beelder.processor.classbuilder.entities;

import com.beelder.processor.utils.BeelderUtils;
import com.beelder.processor.utils.StringBuilderUtils;

import java.util.Objects;

public class Variable extends Type {
    /**
     * Type of this variable.
     */
    private final String type;
    /**
     * Value of this variable, might be null.
     */
    private final String value;



    public Variable(String type, String key) {
        super(key);

        this.type = type;
        this.value = null;
    }

    public Variable(String type, String key, String value) {
        super(key);

        this.type = type;
        this.value = value;
    }


    @Override
    public String build(final int depth) {
        final StringBuilder variableString = new StringBuilder();
        StringBuilderUtils.indent(variableString, depth);
        getModifiers().stream().map(BeelderUtils::modififerToLowercase).forEach(m -> variableString.append(m).append(" "));
        variableString.append(this.type).append(" ").append(getKey());

        if(Objects.nonNull(this.value)) {
            variableString.append(" = ").append(this.value);
        }

        return variableString.toString();
    }

    public String getType() {
        return this.type;
    }
}
