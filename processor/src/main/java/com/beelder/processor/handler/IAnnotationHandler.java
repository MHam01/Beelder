package com.beelder.processor.handler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Interface for handlers handling specific types of annotations.
 */
public interface IAnnotationHandler {
    /**
     * Checks if this handler is able to handle the given annotation.
     *
     * @param annotation Annotation to be checked
     * @return True if it can be handled, false otherwise
     */
    boolean canHandle(final TypeElement annotation);

    /**
     * Handles the given annotation.
     *
     * @param annotation Annotation to be handled
     * @param roundEnvironment The current round environment
     * @param processingEnvironment The current processing environment
     */
    void handleAnnotation(final TypeElement annotation, final RoundEnvironment roundEnvironment, final ProcessingEnvironment processingEnvironment);
}
