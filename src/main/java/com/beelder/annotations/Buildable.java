package com.beelder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking a class to generate a designated builder class from.
 * The builder will be put into the same package, the class resides in.
 * <p>
 *     See also {@link BuildingBlock}
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Buildable {
    String QUALIFIED_NAME = "com.beelder.annotations.Buildable";
    String SIMPLE_NAME = "Buildable";
}
