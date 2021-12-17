package com.beelder.processor.utils;

import com.beelder.processor.constants.BeelderConstants;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

public final class ElementUtils {
    private ElementUtils() {
        // BeelderUtils class
    }

    public static String getElementNameQualified(final Element element) {
        return element.toString();
    }

    public static String getElementType(final Element element) {
        if(!ElementKind.METHOD.equals(element.getKind())) {
            return element.asType().toString().replaceAll("\\(|\\)|void", "");
        }

        return element.asType().toString().replaceAll("\\(.*\\)", "");
    }

    public static String getElementNameSimple(final Element element) {
        return element.getSimpleName().toString();
    }

    public static String getBuilderNameFor(Element element) {
        if(!ElementKind.CLASS.equals(element.getKind())) {
            element = element.getEnclosingElement();
        }

        return element.getSimpleName().toString().concat(BeelderConstants.BUILDABLE_CLASS_SUFFIX);
    }
}
