package com.beelder.processor.handler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

public interface IAnnotationHandler {
    boolean canHandle(final TypeElement annotation);

    void handleAnnotation(final TypeElement annotation, final RoundEnvironment roundEnvironment, final ProcessingEnvironment processingEnvironment);
}
