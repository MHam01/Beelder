package com.beelder.processor.handler;

import com.beelder.annotations.Buildable;
import com.beelder.processor.classbuilder.ClazzBuilder;
import com.beelder.processor.classbuilder.entities.Clazz;
import com.beelder.processor.constants.BeelderConstants;
import com.beelder.processor.utils.BeelderUtils;
import com.beelder.processor.utils.ElementUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

public class BuildableHandler implements IAnnotationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BuildableHandler.class);

    @Override
    public boolean canHandle(TypeElement annotation) {
        return annotation.getQualifiedName().contentEquals(Buildable.class.getName());
    }

    @Override
    public void handleAnnotation(TypeElement annotation, RoundEnvironment roundEnvironment, ProcessingEnvironment processingEnvironment) {
        LOG.info("Handling annotation {}...", annotation.getSimpleName());
        roundEnvironment.getElementsAnnotatedWith(annotation).forEach(e -> {
            checkClass(e, processingEnvironment);
            checkConstructors(e, processingEnvironment);

            final Clazz clazz = ClazzBuilder.getRootForName(ElementUtils.getBuilderNameFor(e));
            clazz.setPackageIdent(StringUtils.substringBeforeLast(e.toString(), "."));
            clazz.addVariable(e.toString(), BeelderConstants.BUILDABLE_OBJECT_NAME, "new ".concat(e.toString().concat("()")), PRIVATE);
        });
        LOG.info("Successfully handled annotation {}!", annotation.getSimpleName());
    }

    /**
     * Checks if a given class element is not private nor abstract, in order to
     * be instantiated by the generated builder.
     *
     * @param clazz The class
     * @param procEnv The current processing environment in order to throw compiler error if needed
     */
    private void checkClass(final Element clazz, final ProcessingEnvironment procEnv) {
        if(BeelderUtils.containsAny(clazz.getModifiers(), ABSTRACT, PRIVATE)) {
            LOG.debug("Class {} is private or abstract, throwing compiler error!", clazz.getSimpleName());
            BeelderUtils.messageElementAnnotatedWith(procEnv, Diagnostic.Kind.ERROR, Buildable.SIMPLE_NAME, "but is private or abstract", clazz);
        }
    }

    /**
     * Checks if a given class element contains a public constructor, as this would be bad practice
     * in a class for which a builder should be generated.
     *
     * @param clazz The class
     * @param procEnv The current processing environment in order to throw compiler error if needed
     */
    private void checkConstructors(final Element clazz, final ProcessingEnvironment procEnv) {
        clazz.getEnclosedElements().stream().filter(e -> ElementKind.CONSTRUCTOR.equals(e.getKind())).forEach(e -> {
            if(e.getModifiers().contains(PUBLIC)) {
                LOG.debug("Class {} contains one or more non-private constructors, sending compiler warning!", clazz.getSimpleName());
                BeelderUtils.messageElementAnnotatedWith(procEnv, Diagnostic.Kind.WARNING, Buildable.SIMPLE_NAME, "but contains a public constructor", e);
            }
        });
    }
}
