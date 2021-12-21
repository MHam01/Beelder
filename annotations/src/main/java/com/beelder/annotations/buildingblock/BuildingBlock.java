package com.beelder.annotations.buildingblock;

import com.beelder.annotations.Buildable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking elements of a class to be included into the generated builder.
 * Allowed elements are fields and methods. The annotated element should reside in a class
 * marked with {@link Buildable}!
 * <p>
 *     For customization see {@link NonNull}
 * </p>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface BuildingBlock {
    String QUALIFIED_NAME = "com.beelder.annotations.buildingblock.BuildingBlock";
    String SIMPLE_NAME = "BuildingBlock";
}
