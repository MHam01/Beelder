package com.beelder.processor.constants;

import com.beelder.annotations.Buildable;
import com.beelder.annotations.buildingblock.BuildingBlock;

import java.util.Set;

public final class BeelderConstants {
    private BeelderConstants() {
        // Static class
    }

    public static Set<String> primiteTypes =
            Set.of("byte", "short", "int", "char", "double", "float", "boolean");

    /**
     * Suffix for generated builder classes.
     */
    public static final String BUILDABLE_CLASS_SUFFIX = "Builder";

    /**
     * Name of the object to be built.
     */
    public static final String BUILDABLE_OBJECT_NAME = "object";

    /**
     * Prefix for parameters in setter methods.
     */
    public static final String SETTER_METHOD_PARAM_NAME = "param";

    /**
     * Name of the build method in the generated builder.
     */
    public static final String BUILD_METHOD_NAME = "build";

    /**
     * Helper string for generic processing environment messages.
     */
    public static final String MESSAGE_IN_PROC_ENV_BASE = "%s %s is annotated with @%s, %s!";
}
