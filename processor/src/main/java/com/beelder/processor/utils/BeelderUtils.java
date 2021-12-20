package com.beelder.processor.utils;

import com.beelder.annotations.Buildable;
import com.beelder.processor.constants.BeelderConstants;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
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

    /**
     * Prints a compiler message to the given processing environment, using {@link BeelderConstants#MESSAGE_IN_PROC_ENV_BASE}.
     *
     * @param procEnv The processing env
     * @param msgKind Diagnostic kind of the message
     * @param annotName The name of the annotation throwing this message
     * @param suffix The suffix to be printed after {@link BeelderConstants#MESSAGE_IN_PROC_ENV_BASE}
     * @param element The element source
     */
    public static void messageElementAnnotatedWith(final ProcessingEnvironment procEnv, final Diagnostic.Kind msgKind, final String annotName, final String suffix, final Element element) {
        procEnv.getMessager().printMessage(msgKind, String.format(BeelderConstants.MESSAGE_IN_PROC_ENV_BASE, element.getKind().name().toLowerCase(Locale.ROOT), element, annotName, suffix), element);
    }

    /**
     * Checks the given elements class for the given annotation, if the element is
     * not a class, checks the enclosing element.
     *
     * @param annot The annotation class to look for
     * @param element The source element
     * @param <T> The annotation type
     * @return The instance of the annotation if found, null otherwise
     */
    public static <T extends Annotation> T fetchAnnotationForEnclosing(final Class<T> annot, final Element element) {
        if(ElementKind.CLASS.equals(element.getKind())) {
            return element.getAnnotation(annot);
        }

        return element.getEnclosingElement().getAnnotation(annot);
    }
}
