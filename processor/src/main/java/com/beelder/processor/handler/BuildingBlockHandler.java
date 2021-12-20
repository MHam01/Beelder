package com.beelder.processor.handler;

import com.beelder.annotations.Buildable;
import com.beelder.annotations.BuildingBlock;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import java.util.Objects;
import java.util.Set;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.STATIC;

public final class BuildingBlockHandler implements IAnnotationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BuildingBlockHandler.class);

    @Override
    public boolean canHandle(TypeElement annotation) {
        return annotation.getQualifiedName().contentEquals(BuildingBlock.QUALIFIED_NAME);
    }

    @Override
    public void handleAnnotation(TypeElement annotation, RoundEnvironment roundEnvironment,
                                 ProcessingEnvironment processingEnvironment) {
        LOG.info("Handling annotation {}...", annotation.getSimpleName());
        roundEnvironment.getElementsAnnotatedWith(annotation).forEach(e -> handleAnnotatedElement(e, processingEnvironment));
        LOG.info("Successfully handled annotation {}!", annotation.getSimpleName());
    }

    private void handleAnnotatedElement(final Element element, final ProcessingEnvironment procEnv) {
        if(!checkEnclosingClass(element, procEnv)) {
            return;
        }

        if(ElementKind.FIELD.equals(element.getKind())) {
            LOG.debug("Handling field {} annotated with {}...", element.getSimpleName(), BuildingBlock.SIMPLE_NAME);
            handleField(element, procEnv);
        } else if(ElementKind.METHOD.equals(element.getKind())) {
            LOG.debug("Handling method {} annotated with {}...", element.getSimpleName(), BuildingBlock.SIMPLE_NAME);
            handleMethod(element, procEnv);
        }
    }

    /**
     * Checks if the enclosing class is annotated with {@link Buildable}. If it is not builder generation will be
     * aborted, because the respective {@link Clazz} will be missing!
     *
     * @param fieldOrMethod Field or method in the class to be checked
     * @return True if handling can continue, false otherwise
     */
    private boolean checkEnclosingClass(final Element fieldOrMethod, final ProcessingEnvironment procEnv) {
        if(Objects.nonNull(BeelderUtils.fetchAnnotationForEnclosing(Buildable.class, fieldOrMethod))) {
            return true;
        }

        LOG.error("Enclosing class of field or method {} is not annotated with @{}, builder generation will be aborted!",
                fieldOrMethod.getSimpleName(), Buildable.SIMPLE_NAME);
        BeelderUtils.messageElementAnnotatedWith(procEnv, Diagnostic.Kind.WARNING, BuildingBlock.SIMPLE_NAME,
                "but enclosing class is not annotated with @" + Buildable.SIMPLE_NAME + "! Builder will not be generated", fieldOrMethod);
        return false;
    }

    /**
     * Checks if a given field is either final (not suitable to be modified by a builder),
     * public/package-private (assign the variable directly in the generated builder)
     * or private (checks for a setter method in the original class)!
     */
    private void handleField(final Element field, final ProcessingEnvironment procEnv) {
        final String enclosingClazz = ElementUtils.getBuilderNameFor(field);
        final Set<Modifier> modifiers = field.getModifiers();

        if(BeelderUtils.containsAny(modifiers, FINAL, STATIC)) {
            LOG.debug("Field is final, throwing compiler error!");
            BeelderUtils.messageElementAnnotatedWith(procEnv, Diagnostic.Kind.ERROR, BuildingBlock.SIMPLE_NAME,
                    "but is final or static", field);
        } else if(BeelderUtils.containsNone(modifiers, PRIVATE, PROTECTED)) {
            LOG.debug("Field is accessible, adding new method to builder root!");
            addPublicVarAssign(ClazzBuilder.getRootForName(enclosingClazz), field);
        } else {
            final Element setterMethod = lookForSetterMethod(field.getEnclosingElement(), getSetterMethod(field));

            if(Objects.isNull(setterMethod) &&
                    BeelderUtils.fetchAnnotationForEnclosing(Buildable.class, field).writeWithReflection()) {
                handleNullSetter(field, enclosingClazz, procEnv);
                return;
            }

            LOG.debug("Found setter method for field {}...", field.getSimpleName());
            handleMethod(setterMethod, procEnv);
        }
    }

    private void handleNullSetter(final Element source, final String enclosingClazz, final ProcessingEnvironment procEnv) {
        if(BeelderUtils.fetchAnnotationForEnclosing(Buildable.class, source).writeWithReflection()) {
            LOG.debug("Field is not accessible, but setting via reflection was enabled for this builder!");
            addReflectionSettingMethod(source, enclosingClazz);
        } else {
            LOG.debug("Field is private and does not contain a valid setter method, throwing compiler error!");
            BeelderUtils.messageElementAnnotatedWith(procEnv, Diagnostic.Kind.ERROR,
                    BuildingBlock.SIMPLE_NAME, "but it's enclosing class does not contain a valid setter method", source);
        }
    }

    /**
     * Is only called if reflection is enabled for the generated builder. Generates a try-catch
     * block to set the given field in the source object via reflection and adds the method for
     * that to the builder root.
     */
    private void addReflectionSettingMethod(final Element field, final String builderName) {
        final String fieldNameSimple = ElementUtils.getElementNameSimple(field);
        final Clazz clazz = ClazzBuilder.getRootForName(builderName);
        final Variable objectVar = clazz.getVariableFor(BeelderConstants.BUILDABLE_OBJECT_NAME);

        if(clazz.containsMethod(getSetterMethod(field))) {
            return;
        }

        final StatementBuilder.TryBlock theTry = StatementBuilder.createTryBlock();
        theTry.addLine(String.format(
                "java.lang.reflect.Field field = %s.class.getDeclaredField(\"%s\");", objectVar.getType(), fieldNameSimple));
        theTry.addLine("field.setAccessible(true);");
        theTry.addLine(String.format(
                "field.set(this.%s, %s);", BeelderConstants.BUILDABLE_OBJECT_NAME, BeelderConstants.SETTER_METHOD_PARAM_NAME));
        theTry.addLine("field.setAccessible(false);");
        theTry.addLineToCatchClause("", "NoSuchFieldException | IllegalAccessException");

        final Method method = clazz.fetchMethod(getSetterMethod(field));
        final Variable param = new Variable(ElementUtils.getElementType(field), BeelderConstants.SETTER_METHOD_PARAM_NAME);
        method.setReturnType(builderName);
        method.addParameter(param);
        method.addLine(theTry.build(2));
        method.addReturnStatement("this");
    }

    /**
     * Checks if a given method is either private/protected (not suitable to be called by a builder),
     * or not returning void (should probably not be called without considering the returned object),
     * else adds this method to the builder.
     */
    private void handleMethod(final Element methodEl, final ProcessingEnvironment procEnv) {
        if(!checkMethodMods(methodEl, procEnv)) {
            return;
        }

        final String enclosingClazz = ElementUtils.getBuilderNameFor(methodEl);
        final Clazz clazz = ClazzBuilder.getRootForName(enclosingClazz);
        final String methodName = ElementUtils.getElementNameSimple(methodEl);

        final ExecutableElement methodExecEl = getAsMethod(methodEl, procEnv);
        if(clazz.containsMethod(methodName) || Objects.isNull(methodExecEl)) {
            return;
        }

        createMethodCallForClazz(clazz, methodName, methodExecEl);
    }

    private boolean checkMethodMods(final Element method, final ProcessingEnvironment procEnv) {
        if(BeelderUtils.containsAny(method.getModifiers(), PRIVATE, PROTECTED)) {
            LOG.debug("Method is inaccessible, throwing compiler warning!");
            BeelderUtils.messageElementAnnotatedWith(
                    procEnv, Diagnostic.Kind.ERROR, BuildingBlock.SIMPLE_NAME, "but is not accessible from outside the class", method);
            return false;
        }

        return true;
    }

    private ExecutableElement getAsMethod(final Element method, final ProcessingEnvironment procEnv) {
        final ExecutableElement methodExecEl = ElementUtils.asMethod(method);

        if(Objects.nonNull(methodExecEl) && !TypeKind.VOID.equals(methodExecEl.getReturnType().getKind())) {
            LOG.debug("Methods return type is not void, sending compiler warning!");
            BeelderUtils.messageElementAnnotatedWith(
                    procEnv, Diagnostic.Kind.WARNING, BuildingBlock.SIMPLE_NAME,
                    "and it's return statement will be ignored in the generated builder", method);
        }

        return methodExecEl;
    }

    private void createMethodCallForClazz(final Clazz clazz, final String methodName, final ExecutableElement methodEl) {
        final Method method = clazz.fetchMethod(methodName);
        method.setReturnType(clazz.getKey());
        methodEl.getParameters().stream().map(Variable::from).forEach(var -> {
            var.setKey(BeelderConstants.SETTER_METHOD_PARAM_NAME);
            method.addParameter(var);
        });

        final String[] parametersAsStr = method.getParameters().stream().map(Variable::getKey).toArray(String[]::new);
        method.addLine(StatementBuilder.createMethodCall("this." + BeelderConstants.BUILDABLE_OBJECT_NAME, methodName, parametersAsStr));
        method.addReturnStatement("this");
    }

    /**
     * Field is public/package-private -> create direct assignment.
     */
    private void addPublicVarAssign(final Clazz clazz, final Element element) {
        final String fieldName = ElementUtils.getElementNameSimple(element);
        final String methodName = getSetterMethod(element);

        if(clazz.containsMethod(methodName)) {
            return;
        }

        final Method method = clazz.fetchMethod(methodName);
        final Variable param = new Variable(ElementUtils.getElementType(element), BeelderConstants.SETTER_METHOD_PARAM_NAME);
        method.setReturnType(clazz.getKey());
        method.addParameter(param);
        method.addLine(StatementBuilder.createAssignment("this." + BeelderConstants.BUILDABLE_OBJECT_NAME, fieldName, param.getKey()));
        method.addReturnStatement("this");
    }

    private Element lookForSetterMethod(final Element element, final String methodName) {
        return element.getEnclosedElements().stream()
                .filter(e -> e.getSimpleName().contentEquals(methodName))
                .findFirst().orElse(null);
    }

    /**
     * Generates the setter method name for a given element, e.g.
     * getSetterMethod(field) = "setField".
     *
     * @return setter method as a string
     */
    private String getSetterMethod(final Element element) {
        return "set" + StringUtils.capitalize(ElementUtils.getElementNameSimple(element));
    }
}
