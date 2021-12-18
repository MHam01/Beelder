package com.beelder.processor.handler;

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
import org.apache.commons.lang3.tuple.Pair;
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

public class BuildingBlockHandler implements IAnnotationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BuildingBlockHandler.class);

    @Override
    public boolean canHandle(TypeElement annotation) {
        return annotation.getQualifiedName().contentEquals(BuildingBlock.class.getName());
    }

    @Override
    public void handleAnnotation(TypeElement annotation, RoundEnvironment roundEnvironment, ProcessingEnvironment processingEnvironment) {
        LOG.info("Handling annotation {}...", annotation.getSimpleName());
        roundEnvironment.getElementsAnnotatedWith(annotation).forEach(e -> handleAnnotatedElement(e, processingEnvironment));
        LOG.info("Successfully handled annotation {}!", annotation.getSimpleName());
    }

    private void handleAnnotatedElement(final Element element, final ProcessingEnvironment procEnv) {
        if(ElementKind.FIELD.equals(element.getKind())) {
            LOG.debug("Handling field {} annotated with {}...", element.getSimpleName(), BuildingBlock.SIMPLE_NAME);
            handleField(element, procEnv);
        } else if(ElementKind.METHOD.equals(element.getKind())) {
            LOG.debug("Handling method {} annotated with {}...", element.getSimpleName(), BuildingBlock.SIMPLE_NAME);
            handleMethod(element, procEnv);
        }
    }

    /**
     * Checks if a given field is either final (not suitable to be modified by a builder),
     * public/package-private (assign the variable directly in the generated builder)
     * or private (checks for a setter method in the original class)!
     *
     * @param field The annotated field
     * @param procEnv The current processing environment in order to throw compiler error if needed
     */
    private void handleField(final Element field, final ProcessingEnvironment procEnv) {
        final String enclosingClazz = ElementUtils.getBuilderNameFor(field);
        final Set<Modifier> modifiers = field.getModifiers();

        if(modifiers.contains(FINAL)) {
            LOG.debug("Field is final, throwing compiler error!");
            BeelderUtils.messageElementAnnotatedWith(procEnv, Diagnostic.Kind.ERROR, BuildingBlock.SIMPLE_NAME, "but is final", field);
        } else if(BeelderUtils.containsNone(modifiers, PRIVATE, PROTECTED)) {
            LOG.debug("Field is accessible, adding new method to builder root!");
            addPublicVarAssign(ClazzBuilder.getRootForName(enclosingClazz), field);
        } else {
            final Element setterMethod = lookForSetterMethod(field.getEnclosingElement(), getSetterMethod(field));
            if(Objects.isNull(setterMethod)) {
                LOG.debug("Field is private and does not contain a valid setter method, throwing compiler error!");
                BeelderUtils.messageElementAnnotatedWith(procEnv, Diagnostic.Kind.ERROR, BuildingBlock.SIMPLE_NAME, "but it's enclosing class does not contain a valid setter method", field);
                return;
            }

            LOG.debug("Found setter method for field {}...", field.getSimpleName());
            handleMethod(setterMethod, procEnv);
        }
    }

    /**
     * Checks if a given method is either private/protected (not suitable to be called by a builder),
     * or not returning void (should probably not be called without considering the returned object),
     * else adds this method to the builder.
     *
     * @param methodEl The method as an element
     * @param procEnv The current processing environment
     */
    private void handleMethod(final Element methodEl, final ProcessingEnvironment procEnv) {
        final String methodName = ElementUtils.getElementNameSimple(methodEl);
        final String enclosingClazz = ElementUtils.getBuilderNameFor(methodEl);
        final Set<Modifier> modifiers = methodEl.getModifiers();

        if(BeelderUtils.containsAny(modifiers, PRIVATE, PROTECTED)) {
            LOG.debug("Method is inaccessible, throwing compiler warning!");
            BeelderUtils.messageElementAnnotatedWith(procEnv, Diagnostic.Kind.ERROR, BuildingBlock.SIMPLE_NAME, "but is not accessible from outside the class", methodEl);
            return;
        }

        final ExecutableElement methodExecEl = ElementUtils.asMethod(methodEl);
        if(Objects.isNull(methodExecEl)) {
            // Should never occur, but just to be sure
            return;
        } else if(!TypeKind.VOID.equals(methodExecEl.getReturnType().getKind())) {
            LOG.debug("Methods return type is not void, sending compiler warning!");
            BeelderUtils.messageElementAnnotatedWith(procEnv, Diagnostic.Kind.WARNING, BuildingBlock.SIMPLE_NAME, "and it's return statement will be ignored in the generated builder", methodEl);
        }

        final Clazz clazz = ClazzBuilder.getRootForName(enclosingClazz);
        final Method method = clazz.fetchMethod(ElementUtils.getElementNameSimple(methodEl));
        method.setReturnType(enclosingClazz);
        methodExecEl.getParameters().stream().map(Variable::from).forEach(method::addParameter);

        final String[] parametersAsStr = method.getParameters().stream().map(Variable::getKey).toArray(String[]::new);
        method.addLine(StatementBuilder.createMethodCall(BeelderConstants.BUILDABLE_OBJECT_NAME, methodName, parametersAsStr));
        method.addReturnStatement("this");
    }

    /**
     * Field is public/package-private -> create direct assignment.
     *
     * @param clazz The root clazz object
     * @param element The field
     */
    private void addPublicVarAssign(final Clazz clazz, final Element element) {
        final String fieldName = ElementUtils.getElementNameSimple(element);
        final String methodName = getSetterMethod(element);
        final Method method = clazz.fetchMethod(methodName);

        final Variable param = new Variable(ElementUtils.getElementType(element), BeelderConstants.SETTER_METHOD_PARAM_NAME + method.parameterNum());
        method.setReturnType(clazz.getKey());
        method.addParameter(param);
        method.addLine(StatementBuilder.createAssignment(BeelderConstants.BUILDABLE_OBJECT_NAME, fieldName, param.getKey()));
        method.addLine("this");
    }

    private Element lookForSetterMethod(final Element element, final String methodName) {
        return element.getEnclosedElements().stream().filter(e -> e.getSimpleName().contentEquals(methodName)).findFirst().orElse(null);
    }

    /**
     * Generates the setter method name for a given element, e.g.
     * getSetterMethod(field) = "setField".
     *
     * @param element The element
     * @return setter method as a string
     */
    private String getSetterMethod(final Element element) {
        return "set".concat(StringUtils.capitalize(ElementUtils.getElementNameSimple(element)));
    }
}
