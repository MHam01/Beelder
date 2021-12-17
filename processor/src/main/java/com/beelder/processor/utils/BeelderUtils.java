package com.beelder.processor.utils;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public final class BeelderUtils {
    private BeelderUtils() {
        // Util class
    }

    /**
     * Helper method to check if none of the given elements are present in
     * the given collection.
     *
     * @param col The collection
     * @param check Elements to check
     * @return true if no elements are present, false otherwise
     */
    @SafeVarargs
    public static <T> boolean containsNone(final Collection<T> col, final T... check) {
        return !containsAny(col, check);
    }

    /**
     * Helper method to check if any of the given elements are present in the
     * given collection.
     *
     * @param col The collection
     * @param check Elements to check
     * @return true if at least one element is present, false otherwise
     */
    @SafeVarargs
    public static <T> boolean containsAny(final Collection<T> col, final T... check) {
        return Arrays.stream(check).anyMatch(col::contains);
    }

    /**
     * @param modifier Modifier to be turned into a string
     * @return Lowercase name of the given modifier
     */
    public static String modififerToLowercase(final Modifier modifier) {
        return modifier.name().toLowerCase(Locale.ROOT);
    }
}
