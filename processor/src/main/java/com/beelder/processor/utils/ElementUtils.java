package com.beelder.processor.utils;

import com.beelder.processor.constants.BeelderConstants;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

public final class ElementUtils {
    private ElementUtils() {
        // Util class
    }

    /**
     * @return The qualified name of the given element, usually "package + simple_name"
     */
    public static String getElementNameQualified(final Element element) {
        return element.toString();
    }

    /**
     * @return The type of the given element, if a method is given the return type
     */
    public static String getElementType(final Element element) {
        if(!ElementKind.METHOD.equals(element.getKind())) {
            return element.asType().toString().replaceAll("\\(|\\)|void", "");
        }

        return element.asType().toString().replaceAll("\\(.*\\)", "");
    }

    /**
     * @return The simple name of the given element
     */
    public static String getElementNameSimple(final Element element) {
        return element.getSimpleName().toString();
    }

    /**
     * @return The name of the builder, the given element is related to
     */
    public static String getBuilderNameFor(Element element) {
        if(!ElementKind.CLASS.equals(element.getKind())) {
            element = element.getEnclosingElement();
        }

        return element.getSimpleName().toString().concat(BeelderConstants.BUILDABLE_CLASS_SUFFIX);
    }

    /**
     * Casts the given element into an {@link ExecutableElement}.
     *
     * @param element The element to be cast
     * @return The cast element if possible, null otherwise
     */
    public static ExecutableElement asMethod(final Element element) {
        try {
            return (ExecutableElement) element;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Generates the setter method name for a given element, e.g.
     * getSetterMethod(field) = "setField".
     *
     * @return setter method as a string
     */
    public static String setterMethodFrom(final Element element) {
        return "set" + StringUtils.capitalize(ElementUtils.getElementNameSimple(element));
    }
}
