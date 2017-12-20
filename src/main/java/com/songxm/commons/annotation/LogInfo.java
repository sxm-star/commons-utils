package com.songxm.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LogInfo {
    String value() default "";

    String[] params() default {};

    boolean replace() default true;

    String errorKey() default "";

    String[] errorParams() default {};

    boolean logError() default true;
}