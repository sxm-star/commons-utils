package com.songxm.commons.validator;

import com.songxm.commons.annotation.Mobile;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class MobileValidator implements ConstraintValidator<Mobile, String> {
    private final Pattern PATTERN_MOBILE = Pattern.compile("1\\d{10}");

    public MobileValidator() {
    }

    @Override
    public void initialize(Mobile mobile) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return !StringUtils.isNotBlank(s) || this.PATTERN_MOBILE.matcher(s).matches();
    }
}