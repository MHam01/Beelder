package com.beelder.processor.handler;

import com.beelder.annotations.Buildable;
import com.beelder.processor.constants.BeelderConstants;
import com.beelder.processor.classbuilder.ClazzBuilder;
import com.beelder.processor.classbuilder.entities.Clazz;
import com.beelder.processor.utils.BeelderUtils;
import com.beelder.processor.utils.ElementUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class BuildableHandler implements IAnnotationHandler {
    @Override
    public boolean canHandle(TypeElement annotation) {
        return annotation.getQualifiedName().contentEquals(Buildable.QUALIFIED_NAME);
    }

    @Override
    public void handleAnnotation(TypeElement annotation, RoundEnvironment roundEnvironment, ProcessingEnvironment processingEnvironment) {
        roundEnvironment.getElementsAnnotatedWith(annotation).forEach(e -> {
            checkConstructors(e, processingEnvironment);
            final Clazz clazz = ClazzBuilder.getRootForName(ElementUtils.getBuilderNameFor(e));
            clazz.setPackageIdent(StringUtils.substringBeforeLast(e.toString(), "."));
            clazz.addVariable(e.toString(), BeelderConstants.BUILDABLE_OBJECT_NAME, "new ".concat(e.toString().concat("()")), Modifier.PRIVATE);
        });
    }

    private void checkClass(final Element clazz, final ProcessingEnvironment processingEnvironment) {
        if(BeelderUtils.containsAny(clazz.getModifiers(), Modifier.ABSTRACT, Modifier.PRIVATE)) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Class " + clazz + " is annotated with @Buildable, but is private or abstract!");
        }
    }

    private void checkConstructors(final Element clazz, final ProcessingEnvironment processingEnvironment) {
        clazz.getEnclosedElements().stream().filter(e -> ElementKind.CONSTRUCTOR.equals(e.getKind())).forEach(e -> {
            if(e.getModifiers().contains(Modifier.PUBLIC)) {
                processingEnvironment.getMessager().printMessage(
                        Diagnostic.Kind.MANDATORY_WARNING, "Class " + clazz + " is annotated with @Buildable, but contains a public constructor!");
            }
        });
    }
}
