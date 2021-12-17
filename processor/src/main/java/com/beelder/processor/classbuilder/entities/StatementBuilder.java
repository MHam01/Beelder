package com.beelder.processor.classbuilder.entities;

public final class StatementBuilder {
    private StatementBuilder() {
        // Util class for creating statements, mostly used in Methods
    }


    /**
     * Creates a new assignment of the form "source.assign = to;".
     *
     * @return Assignment as string
     */
    public static String createAssignment(final String source, final String assign, final String to) {
        return String.format("%s.%s = %s;", source, assign, to);
    }

    /**
     * Creates a new method call of the form "source.method(param0, param1, ...);".
     *
     * @return Call as string
     */
    public static String createMethodCall(final String source, final String method, final String... params) {
        return String.format("%s.%s(%s);", source, method, String.join(", ", params));
    }
}
