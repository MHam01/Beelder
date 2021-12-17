package com.beelder.processor.handler;

import com.beelder.processor.classbuilder.ClazzBuilder;
import com.beelder.processor.classbuilder.entities.Clazz;
import com.beelder.processor.classbuilder.entities.Method;
import com.beelder.processor.classbuilder.entities.Variable;
import com.beelder.processor.constants.BeelderConstants;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClazzBuildingHandler implements IAnnotationHandler {
    private final AtomicBoolean handleOnce = new AtomicBoolean(true);

    @Override
    public boolean canHandle(TypeElement annotation) {
        return handleOnce.getAndSet(false);
    }

    @Override
    public void handleAnnotation(TypeElement annotation, RoundEnvironment roundEnvironment, ProcessingEnvironment processingEnvironment) {
        ClazzBuilder.fetchAllClazzes().stream().peek(this::addBuildMethodTo).forEach(c -> writeClazzToSourceFile(c, processingEnvironment));
    }

    /**
     * Adds the building method to the given clazz object.
     *
     * @param clazz The clazz
     */
    private void addBuildMethodTo(final Clazz clazz) {
        final Method method = clazz.fetchMethod(BeelderConstants.BUILD_METHOD_NAME);
        final Variable builds = clazz.getVariables().stream().filter(var -> BeelderConstants.BUILDABLE_OBJECT_NAME.equals(var.getKey())).findFirst().orElse(null);
        if(Objects.isNull(builds)) {
            return;
        }

        method.setReturnType(builds.getType());
        method.addModifier(Modifier.PUBLIC);
        method.addLine("return ".concat(builds.getKey().concat(";")));
    }

    /**
     * Tries to write the string representation of the given class to a new source file.
     *
     * @param clazz The clazz
     * @param procEnv The processing environment to add the new source file to
     */
    private void writeClazzToSourceFile(final Clazz clazz, final ProcessingEnvironment procEnv) {
        final JavaFileObject builderClass = createSourceFile(clazz.getKey(), procEnv);
        if(Objects.isNull(builderClass)) {
            return;
        }

        try(final PrintWriter writer = new PrintWriter(builderClass.openOutputStream())) {
            writer.println(clazz.build());
            writer.flush();
        } catch (IOException e) {

        }
    }

    /**
     * @return A new source file for the given name, null if it couldn't be created
     */
    private JavaFileObject createSourceFile(final String name, final ProcessingEnvironment procEnv) {
        try {
            return procEnv.getFiler().createSourceFile(name);
        } catch (IOException e) {
            return null;
        }
    }
}