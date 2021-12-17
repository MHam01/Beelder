package com.beelder.processor.utils;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public final class BeelderUtils {
    private BeelderUtils() {
        // Utils class
    }

    @SafeVarargs
    public static <T> boolean containsNone(final Collection<T> col, final T... check) {
        return !containsAny(col, check);
    }

    @SafeVarargs
    public static <T> boolean containsAny(final Collection<T> col, final T... check) {
        return Arrays.stream(check).anyMatch(col::contains);
    }

    public static String modififerToLowercase(final Modifier modifier) {
        return modifier.name().toLowerCase(Locale.ROOT);
    }
}
