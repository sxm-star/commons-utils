package com.songxm.commons.validator;

import com.songxm.commons.annotation.Ip;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class IpValidator implements ConstraintValidator<Ip, String> {
    private final Pattern PATTERN_IP = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");

    public IpValidator() {
    }
    @Override
    public void initialize(Ip ip) {
    }
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return !StringUtils.isNotBlank(s) || this.PATTERN_IP.matcher(s).matches();
    }
}