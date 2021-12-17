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

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Objects;
import java.util.Set;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;

public class BuildingBlockHandler implements IAnnotationHandler {
    @Override
    public boolean canHandle(TypeElement annotation) {
        return annotation.getQualifiedName().contentEquals(BuildingBlock.class.getName());
    }

    @Override
    public void handleAnnotation(TypeElement annotation, RoundEnvironment roundEnvironment, ProcessingEnvironment processingEnvironment) {
        roundEnvironment.getElementsAnnotatedWith(annotation).forEach(e -> handleAnnotatedElement(e, processingEnvironment));
    }

    private void handleAnnotatedElement(final Element element, final ProcessingEnvironment procEnv) {
        if(ElementKind.FIELD.equals(element.getKind())) {
            handleField(element, procEnv);
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
            procEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field " + field + " is annotated with @BuildingBlock, but is final!", field);
        } else if(BeelderUtils.containsNone(modifiers, PRIVATE, PROTECTED)) {
            addPublicVarAssign(ClazzBuilder.getRootForName(enclosingClazz), field);
        } else {
            final Element setterMethod = checkForSetterMethod(field.getEnclosingElement(), getSetterMethod(field));
            if(!checkSetterMethod(field, setterMethod, procEnv)){
                return;
            }

            addSetterMethodCall(ClazzBuilder.getRootForName(enclosingClazz), setterMethod, field);
        }
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

        final Pair<Method, Variable> method = createMethodBase(methodName, clazz, element);
        method.getLeft().addLine(StatementBuilder.createAssignment(BeelderConstants.BUILDABLE_OBJECT_NAME, fieldName, method.getRight().getKey()));
        method.getLeft().addLine(BeelderConstants.RETURN_THIS_STR);
    }

    /**
     * Field is private, but contains a setter method -> create method call.
     *
     * @param clazz The root clazz object
     * @param method The setter method
     * @param fromField The field which is set
     */
    private void addSetterMethodCall(final Clazz clazz, final Element method, final Element fromField) {
        final String methodName = ElementUtils.getElementNameSimple(method);

        final Pair<Method, Variable> methodObj = createMethodBase(methodName, clazz, fromField);
        methodObj.getLeft().addLine(StatementBuilder.createMethodCall(BeelderConstants.BUILDABLE_OBJECT_NAME, methodName, methodObj.getRight().getKey()));
        methodObj.getLeft().addLine(BeelderConstants.RETURN_THIS_STR);
    }

    private Pair<Method, Variable> createMethodBase(final String name, final Clazz clazz, final Element sourceField) {
        final Method method = clazz.fetchMethod(name);

        final Variable param = new Variable(ElementUtils.getElementType(sourceField), BeelderConstants.SETTER_METHOD_PARAM_NAME + method.parameterNum());
        method.setReturnType(clazz.getKey());
        method.addParameter(param);

        return Pair.of(method, param);
    }

    private Element checkForSetterMethod(final Element element, final String methodName) {
        return element.getEnclosedElements().stream().filter(e -> e.getSimpleName().contentEquals(methodName)).findFirst().orElse(null);
    }

    /**
     * Checks if a found setter method is either null (no setter method cannot be handled)
     * or private/protected (inaccessible methods cannot be called).
     *
     * @param source The source field
     * @param setterMethod The setter method found
     * @param procEnv The current processing environment in order to throw compiler error if needed
     * @return True if this setter method can be used, false otherwise
     */
    private boolean checkSetterMethod(final Element source, final Element setterMethod, final ProcessingEnvironment procEnv) {
        if(Objects.isNull(setterMethod)) {
            procEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field " + source + " is annotated with @BuildingBlock, but it's enclosing class does not contain a valid setter method!", source);
            return false;
        } else if(BeelderUtils.containsAny(setterMethod.getModifiers(), PRIVATE, PROTECTED)) {
            procEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field " + source + " is annotated with @BuildingBlock, but it's setter method is private!", source);
            return false;
        }

        return true;
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
