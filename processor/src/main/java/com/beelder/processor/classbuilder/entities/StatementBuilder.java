package com.beelder.processor.classbuilder.entities;

public final class StatementBuilder {
    private StatementBuilder() {
        // Util class
    }


    public static String createAssignment(final String source, final String assign, final String to) {
        return String.format("%s.%s = %s;", source, assign, to);
    }

    public static String createMethodCall(final String source, final String method, final String... params) {
        return String.format("%s.%s(%s);", source, method, String.join(", ", params));
    }
}
