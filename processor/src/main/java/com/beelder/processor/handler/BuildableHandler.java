package com.beelder.processor.handler;

import com.beelder.annotations.Buildable;
import com.beelder.annotations.Excluded;
import com.beelder.processor.classbuilder.ClazzBuilder;
import com.beelder.processor.classbuilder.entities.Clazz;
import com.beelder.processor.classbuilder.entities.Method;
import com.beelder.processor.classbuilder.entities.StatementBuilder;
import com.beelder.processor.classbuilder.entities.Variable;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.*;

public final class BuildableHandler implements IAnnotationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BuildableHandler.class);

    @Override
    public boolean canHandle(TypeElement annotation) {
        return annotation.getQualifiedName().contentEquals(Buildable.QUALIFIED_NAME);
    }

    @Override
    public void handleAnnotation(TypeElement annotation, RoundEnvironment roundEnvironment,
                                 ProcessingEnvironment processingEnvironment) {
        LOG.info("Handling annotation {}...", annotation.getSimpleName());
        roundEnvironment.getElementsAnnotatedWith(annotation).forEach(e -> {
            checkClass(e, processingEnvironment);

            final Clazz clazz = ClazzBuilder.getRootForName(ElementUtils.getBuilderNameFor(e));
            final String classNameQual = ElementUtils.getElementNameQualified(e);
            clazz.setPackageIdent(StringUtils.substringBeforeLast(classNameQual, "."));
            clazz.addVariable(classNameQual, BeelderConstants.BUILDABLE_OBJECT_NAME, null, PRIVATE);

            addConstructorsToClass(clazz, e, processingEnvironment);
        });
        LOG.info("Successfully handled annotation {}!", annotation.getSimpleName());
    }

    /**
     * Checks if a given class element is not private nor abstract, in order to
     * be instantiated by the generated builder.
     */
    private void checkClass(final Element clazz, final ProcessingEnvironment procEnv) {
        if(BeelderUtils.containsAny(clazz.getModifiers(), ABSTRACT, PRIVATE)) {
            LOG.debug("Class {} is private or abstract, throwing compiler error!", clazz.getSimpleName());
            BeelderUtils.messageElementAnnotatedWith(procEnv, Diagnostic.Kind.ERROR, Buildable.SIMPLE_NAME, "but is private or abstract", clazz);
        }
    }

    /**
     * Adds all constructors of the current class element to the respective clazz object, depending on if
     * reflection is enabled for the current element
     */
    private void addConstructorsToClass(final Clazz clazz, final Element classElement, final ProcessingEnvironment procEnv) {
        final String sourceNameQual = ElementUtils.getElementNameQualified(classElement);

        final Map<Boolean, List<Element>> groupedByPublic = classElement.getEnclosedElements().stream()
                .filter(BuildableHandler::isConstructor)
                .filter(con -> Objects.isNull(con.getAnnotation(Excluded.class)))
                .collect(Collectors.partitioningBy(con -> BeelderUtils.containsNone(con.getModifiers(), PRIVATE, PROTECTED)));

        final boolean reflectionEnabled = BeelderUtils.fetchAnnotationForEnclosing(Buildable.class, classElement).writeWithReflection();
        if(groupedByPublic.get(true).isEmpty() && !reflectionEnabled) {
            LOG.error("Found no accessible constructors for class {}, throwing compiler error!", classElement);
            BeelderUtils.messageElementAnnotatedWith(
                    procEnv, Diagnostic.Kind.ERROR, Buildable.SIMPLE_NAME, "but contains no accessible constructor", classElement);
            return;
        }

        if(groupedByPublic.get(true).stream().anyMatch(con -> con.getModifiers().contains(PUBLIC))) {
            LOG.debug("Class {} contains one or more non-package-private constructors, sending compiler warning!", classElement);
            BeelderUtils.messageElementAnnotatedWith(
                    procEnv, Diagnostic.Kind.WARNING, Buildable.SIMPLE_NAME, "but contains one or more public constructors", classElement);
            LOG.debug("Adding public constructors to generated builder {}...", clazz.getKey());
        }

        groupedByPublic.get(true).forEach(con -> addPublicConstructorToClazz(clazz, sourceNameQual, con));

        if(reflectionEnabled) {
            LOG.debug("Adding private constructors to generated builder {}...", clazz.getKey());
            groupedByPublic.get(false).forEach(con -> addReflectionConstructorToClazz(clazz, sourceNameQual, con));
        }
    }

    @SuppressWarnings("ConstantConditions") // constructor will always be a method
    private void addPublicConstructorToClazz(final Clazz clazz, final String sourceName, final Element constructorEl) {
        final ExecutableElement asMethod = ElementUtils.asMethod(constructorEl);
        final Method constructor = createMethodBase(clazz, asMethod);
        final String parametersStr = constructor.getParameters().stream().map(Variable::getKey).collect(Collectors.joining(", "));
        constructor.addLine(StatementBuilder.createAssignment(
                "this", BeelderConstants.BUILDABLE_OBJECT_NAME, "new ".concat(sourceName).concat("(").concat(parametersStr).concat(")")));

        clazz.addConstructor(constructor);
    }

    @SuppressWarnings("ConstantConditions") // constructor will always be a method
    private void addReflectionConstructorToClazz(final Clazz clazz, final String sourceName, final Element constructorEl) {
        final ExecutableElement asMethod = ElementUtils.asMethod(constructorEl);
        final Method constructor = createMethodBase(clazz, asMethod);
        final String[] parameters = constructor.getParameters().stream().map(Variable::getKey).toArray(String[]::new);
        final String newInstCall = StatementBuilder
                .createAssignToMethodCall("this", BeelderConstants.BUILDABLE_OBJECT_NAME, "constructor", "newInstance", parameters)
                .replace("= ", "= (".concat(sourceName).concat(") "));

        final StatementBuilder.TryBlock theTry = StatementBuilder.createTryBlock();
        theTry.addLine(
                String.format("java.lang.reflect.Constructor<?> constructor = %s.class.getDeclaredConstructor(%s);",
                        sourceName, constructor.getParameters().stream().map(var -> var.getType().concat(".class")).collect(Collectors.joining(", "))));
        theTry.addLine("constructor.setAccessible(true);");
        theTry.addLine(newInstCall);
        theTry.addLine("constructor.setAccessible(false);");
        theTry.addLineToCatchClause("", "NoSuchMethodException", "IllegalAccessException", "InstantiationException", "java.lang.reflect.InvocationTargetException");

        constructor.addLine(theTry.build(2));
        clazz.addConstructor(constructor);
    }

    private Method createMethodBase(final Clazz clazz, final ExecutableElement method) {
        final Method theMethod = new Method("");
        method.getParameters().stream().map(Variable::from)
                .peek(var -> var.setKey(BeelderConstants.SETTER_METHOD_PARAM_NAME + theMethod.parameterNum()))
                .forEach(theMethod::addParameter);
        theMethod.setReturnType(clazz.getKey());

        return theMethod;
    }

    private static boolean isConstructor(final Element element) {
        return ElementKind.CONSTRUCTOR.equals(element.getKind());
    }
}
