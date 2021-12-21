package com.beelder.processor.handler;

import com.beelder.annotations.buildingblock.BuildingBlock;
import com.beelder.annotations.buildingblock.NonNull;
import com.beelder.processor.classbuilder.ClazzBuilder;
import com.beelder.processor.classbuilder.entities.Clazz;
import com.beelder.processor.classbuilder.entities.Method;
import com.beelder.processor.classbuilder.entities.StatementBuilder;
import com.beelder.processor.classbuilder.entities.Variable;
import com.beelder.processor.constants.BeelderConstants;
import com.beelder.processor.utils.BeelderUtils;
import com.beelder.processor.utils.ElementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Objects;
import java.util.stream.Collectors;

public final class NonNullHandler implements IAnnotationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(NonNullHandler.class);

    @Override
    public boolean canHandle(TypeElement annotation) {
        return annotation.getQualifiedName().contentEquals(NonNull.QUALIFIED_NAME);
    }

    @Override
    public void handleAnnotation(TypeElement annotation, RoundEnvironment roundEnvironment, ProcessingEnvironment processingEnvironment) {
        roundEnvironment.getElementsAnnotatedWith(annotation).forEach(e -> {
            checkAnnotatedField(e, processingEnvironment);

            handleAnnotatedElement(e);
        });
    }

    private void checkAnnotatedField(final Element element, final ProcessingEnvironment procEnv) {
        if(Objects.isNull(element.getAnnotation(BuildingBlock.class))) {
            LOG.error("Element {} is annotated with {}, but not with {}, throwing compiler error!",
                    element, NonNull.SIMPLE_NAME, BuildingBlock.SIMPLE_NAME);
            BeelderUtils.messageElementAnnotatedWith(
                    procEnv, Diagnostic.Kind.ERROR, NonNull.SIMPLE_NAME, "but not with @Buildable", element);
        }
    }

    private void handleAnnotatedElement(final Element element) {
        final Clazz clazz = ClazzBuilder.getRootForName(ElementUtils.getBuilderNameFor(element));
        final Method theSetter =
                ElementKind.FIELD.equals(element.getKind()) ?
                        clazz.fetchMethod(ElementUtils.setterMethodFrom(element)) :
                        clazz.fetchMethod(ElementUtils.getElementNameSimple(element));
        final String condition = theSetter.getParameters().stream()
                .filter(var -> !BeelderConstants.PRIMITIVE_TYPES.contains(var.getType()))
                .map(Variable::getKey)
                .map(k -> "java.util.Objects.isNull(" + k + ")")
                .collect(Collectors.joining(" || "));

        if(condition.isEmpty()) {
            return;
        }

        final StatementBuilder.IfBlock ifBlock = StatementBuilder.createIfBlock(condition);

        final NonNull theAnnot = element.getAnnotation(NonNull.class);
        switch (theAnnot.operation()) {
            case NO_OP:
                handleNoOp(ifBlock);
                break;
            case THROW_EXC:
                handleThrowException(ifBlock, theAnnot.message());
                break;
            case PRINT_TO_ERR:
                handlePrintTo(ifBlock, "err", theAnnot.message());
                break;
            case PRINT_TO_STDOUT:
                handlePrintTo(ifBlock, "out", theAnnot.message());
                break;
            case LOG_EXC:
                handleLogException(clazz, ifBlock, theAnnot.message());
                break;
        }

        theSetter.prependLine(ifBlock.build(2));
    }

    private void handleNoOp(final StatementBuilder.IfBlock theIf) {
        theIf.addLine("return this;");
    }

    private void handleThrowException(final StatementBuilder.IfBlock theIf, final String message) {
        theIf.addLine(StatementBuilder.createExceptionThrowing(IllegalArgumentException.class, message));
    }

    private void handlePrintTo(final StatementBuilder.IfBlock theIf, final String stream, final String message) {
        theIf.addLine(String.format("System.%s.println(\"%s\");", stream, message));
        theIf.addLine("return this;");
    }

    private void handleLogException(final Clazz builder, final StatementBuilder.IfBlock theIf, final String message) {
        final String loggerCreationStr = String.format("org.slf4j.LoggerFactory.getLogger(%s.class)", builder.getKey());
        final Variable logVar = new Variable("org.slf4j.Logger", "LOG", loggerCreationStr);
        logVar.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

        builder.addVariable(logVar);
        theIf.addLine(String.format("LOG.error(\"%s\");", message));
        theIf.addLine("return this;");
    }
}
