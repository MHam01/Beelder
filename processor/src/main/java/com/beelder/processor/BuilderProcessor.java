package com.beelder.processor;

import com.beelder.annotations.Buildable;
import com.beelder.annotations.BuildingBlock;
import com.beelder.processor.handler.BuildableHandler;
import com.beelder.processor.handler.BuildingBlockHandler;
import com.beelder.processor.handler.ClazzBuildingHandler;
import com.beelder.processor.handler.IAnnotationHandler;
import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({Buildable.QUALIFIED_NAME, BuildingBlock.QUALIFIED_NAME})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public final class BuilderProcessor extends AbstractProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(BuilderProcessor.class);

    private final List<IAnnotationHandler> handlers = new ArrayList<>();

    {
        handlers.add(new BuildableHandler());
        handlers.add(new BuildingBlockHandler());
        handlers.add(new ClazzBuildingHandler());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        LOG.info("Starting annotation processing...");
        for(final IAnnotationHandler handler:handlers) {
            annotations.stream()
                    .filter(handler::canHandle)
                    .forEach(annot -> handler.handleAnnotation(annot, roundEnv, this.processingEnv));
        }

        LOG.info("Successfully processed annotations!");
        return true;
    }
}
