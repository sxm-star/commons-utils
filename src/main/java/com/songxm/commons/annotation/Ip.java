package com.songxm.commons.annotation;

import com.songxm.commons.validator.IpValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
    validatedBy = {IpValidator.class}
)
public @interface Ip {
    String message() default "无效的ip:{}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}