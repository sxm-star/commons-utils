package com.songxm.commons.validator;
import com.songxm.commons.BaseIdCardUtils;
import com.songxm.commons.annotation.IdCard;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class IdCardValidator implements ConstraintValidator<IdCard, String> {
    public IdCardValidator() {
    }

    @Override
    public void initialize(IdCard idCard) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return !StringUtils.isNotBlank(s) || BaseIdCardUtils.parseIdCard(s, Boolean.valueOf(true)) != null;
    }
}