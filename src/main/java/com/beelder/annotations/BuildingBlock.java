package com.beelder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking elements of a class to be included into the generated builder.
 * Allowed elements are fields and methods.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface BuildingBlock {
    String QUALIFIED_NAME = "com.beelder.annotations.BuildingBlock";
    String SIMPLE_NAME = "BuildingBlock";
}
