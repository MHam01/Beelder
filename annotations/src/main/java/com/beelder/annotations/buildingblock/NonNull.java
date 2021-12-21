package com.beelder.annotations.buildingblock;

/**
 * Used to add a null-check to the annotated field/method (has to be annotated with
 * {@link BuildingBlock}) in the generated builder!
 */
public @interface NonNull {
    String QUALIFIED_NAME = "com.beelder.annotations.buildingblock.NonNull";
    String SIMPLE_NAME = "NonNull";

    /**
     * Resembles the message printed with the operation chosen.
     */
    String message() default "Null argument found";

    Operation operation() default Operation.THROW_EXC;

    enum Operation {
        NO_OP, THROW_EXC, PRINT_TO_ERR, PRINT_TO_STDOUT, LOG_EXC
    }
}
