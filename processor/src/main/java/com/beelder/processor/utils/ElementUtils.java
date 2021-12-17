package com.beelder.processor.utils;

import com.beelder.processor.constants.BeelderConstants;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

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
}
