package com.beelder.processor.utils;

public final class StringBuilderUtils {
    private StringBuilderUtils() {
        // Util class
    }

    /**
     * Adds an indent of the form "repeat * \t" to the given string builder.
     *
     * @param sb The string builder
     * @param repeat The number of tabs
     * @return The string builder
     */
    public static StringBuilder indent(final StringBuilder sb, final int repeat) {
        return sb.append("\t".repeat(repeat));
    }
}
