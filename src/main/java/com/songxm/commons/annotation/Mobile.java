package com.songxm.commons.annotation;

import com.songxm.commons.validator.MobileValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
    validatedBy = {MobileValidator.class}
)
public @interface Mobile {
    String message() default "无效的手机号:{}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}