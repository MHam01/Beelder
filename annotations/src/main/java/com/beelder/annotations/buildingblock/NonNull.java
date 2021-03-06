package com.beelder.annotations.buildingblock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to add a null-check to the annotated field/method (has to be annotated with
 * {@link BuildingBlock}) in the generated builder!
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface NonNull {
    String QUALIFIED_NAME = "com.beelder.annotations.buildingblock.NonNull";
    String SIMPLE_NAME = "NonNull";

    /**
     * Resembles the message printed with the operation chosen.
     */
    String message() default "Null argument found";

    ErrorOperation operation() default ErrorOperation.THROW_EXC;
}
