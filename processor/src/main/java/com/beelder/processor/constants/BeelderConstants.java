package com.beelder.processor.constants;

public final class BeelderConstants {
    private BeelderConstants() {
        // Static class
    }

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
     * Util string for a reoccurring "return this;" in the generated builder.
     */
    public static final String RETURN_THIS_STR = "return this;";
}
