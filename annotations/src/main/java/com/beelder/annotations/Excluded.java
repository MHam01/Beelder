package com.beelder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to excluded constructors from being included in the generated builder. Should
 * be used in combination with {@link Buildable}!
 */
@Target({ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface Excluded {

}
