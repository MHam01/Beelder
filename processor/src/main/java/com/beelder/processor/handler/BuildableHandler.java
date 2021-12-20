package com.beelder.processor.handler;

import com.beelder.annotations.Buildable;
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
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

public final class BuildableHandler implements IAnnotationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BuildableHandler.class);

    @Override
    public boolean canHandle(TypeElement annotation) {
        return annotation.getQualifiedName().contentEquals(Buildable.class.getName());
    }

    @Override
    public void handleAnnotation(TypeElement annotation, RoundEnvironment roundEnvironment,
                                 ProcessingEnvironment processingEnvironment) {
        LOG.info("Handling annotation {}...", annotation.getSimpleName());
        roundEnvironment.getElementsAnnotatedWith(annotation).forEach(e -> {
            checkClass(e, processingEnvironment);
            checkConstructors(e, processingEnvironment);

            final Clazz clazz = ClazzBuilder.getRootForName(ElementUtils.getBuilderNameFor(e));
            final String classNameQual = ElementUtils.getElementNameQualified(e);
            clazz.setPackageIdent(StringUtils.substringBeforeLast(e.toString(), "."));
            clazz.addVariable(e.toString(), BeelderConstants.BUILDABLE_OBJECT_NAME, null, PRIVATE);

            addConstructorsToClass(clazz, classNameQual, e, processingEnvironment);
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
     * Checks if a given class element contains a public constructor, as this would be bad practice
     * in a class for which a builder should be generated.
     */
    private void checkConstructors(final Element clazz, final ProcessingEnvironment procEnv) {
        clazz.getEnclosedElements().stream().filter(e -> ElementKind.CONSTRUCTOR.equals(e.getKind())).forEach(e -> {
            if(e.getModifiers().contains(PUBLIC)) {
                LOG.debug("Class {} contains one or more non-package-private constructors, sending compiler warning!", clazz.getSimpleName());
                BeelderUtils.messageElementAnnotatedWith(
                        procEnv, Diagnostic.Kind.WARNING, Buildable.SIMPLE_NAME, "but contains one or more public constructors", e);
            }
        });
    }

    /**
     * Adds all constructors of the current class element to the respective clazz object, depending on if
     * reflection is enabled for the current element
     */
    private void addConstructorsToClass(final Clazz clazz, final String sourceNameQual, final Element classElement, final ProcessingEnvironment procEnv) {
        final Map<Boolean, List<Element>> groupedByPublic = classElement.getEnclosedElements().stream().filter(BuildableHandler::isConstructor)
                        .collect(Collectors.partitioningBy(con -> BeelderUtils.containsNone(con.getModifiers(), PRIVATE, PROTECTED)));

        final boolean reflectionEnabled = BeelderUtils.fetchAnnotationForEnclosing(Buildable.class, classElement).writeWithReflection();
        if(groupedByPublic.get(true).isEmpty() && !reflectionEnabled) {
            LOG.error("Found no accessible constructors for class {}, throwing compiler error!", classElement);
            BeelderUtils.messageElementAnnotatedWith(
                    procEnv, Diagnostic.Kind.ERROR, Buildable.SIMPLE_NAME, "but contains no accessible constructor", classElement);
            return;
        }

        LOG.debug("Adding public constructors to generated builder {}...", clazz.getKey());
        groupedByPublic.get(true).forEach(con -> addPublicConstructorToClazz(clazz, sourceNameQual, con));

        if(reflectionEnabled) {
            LOG.debug("Adding private constructors to generated builder {}...", clazz.getKey());
            groupedByPublic.get(false).forEach(con -> addReflectionConstructorToClazz(clazz, sourceNameQual, con));
        }
    }

    @SuppressWarnings("ConstantConditions") // constructor will always be a method
    private void addPublicConstructorToClazz(final Clazz clazz, final String sourceName, final Element constructor) {
        final ExecutableElement asMethod = ElementUtils.asMethod(constructor);
        final Method constr = createMethodBase(clazz, asMethod);
        final String parametersStr = constr.getParameters().stream().map(Variable::getKey).collect(Collectors.joining(", "));
        constr.addLine(StatementBuilder.createAssignment(
                "this", BeelderConstants.BUILDABLE_OBJECT_NAME, "new ".concat(sourceName).concat("(").concat(parametersStr).concat(")")));

        clazz.addConstructor(constr);
    }

    @SuppressWarnings("ConstantConditions") // constructor will always be a method
    private void addReflectionConstructorToClazz(final Clazz clazz, final String sourceName, final Element constructor) {
        final ExecutableElement asMethod = ElementUtils.asMethod(constructor);
        final Method constr = createMethodBase(clazz, asMethod);
        final String[] parameters = constr.getParameters().stream().map(Variable::getKey).toArray(String[]::new);
        final String newInstCall = StatementBuilder
                .createAssignToMethodCall("this", BeelderConstants.BUILDABLE_OBJECT_NAME, "constructor", "newInstance", parameters)
                .replace("= ", "= (".concat(sourceName).concat(") "));
        final StatementBuilder.TryBlock theTry = StatementBuilder.createTryBlock();
        theTry.addLine(
                String.format("java.lang.reflect.Constructor<?> constructor = %s.class.getDeclaredConstructor(%s);",
                        sourceName, constr.getParameters().stream().map(var -> var.getType().concat(".class")).collect(Collectors.joining(", "))));
        theTry.addLine("constructor.setAccessible(true);");
        theTry.addLine(newInstCall);
        theTry.addLine("constructor.setAccessible(false);");
        theTry.addLineToCatchClause("", "NoSuchMethodException", "IllegalAccessException", "InstantiationException", "java.lang.reflect.InvocationTargetException");

        constr.addLine(theTry.build(2));
        clazz.addConstructor(constr);
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
