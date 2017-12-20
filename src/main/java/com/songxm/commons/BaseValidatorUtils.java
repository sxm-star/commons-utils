package com.songxm.commons;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Iterator;

public class BaseValidatorUtils {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public BaseValidatorUtils() {
    }

    public static void validate(Object obj) {
        if(obj != null) {
            StringBuilder errors = new StringBuilder();
            Iterator var2 = validator.validate(obj, new Class[0]).iterator();

            while(var2.hasNext()) {
                ConstraintViolation violation = (ConstraintViolation)var2.next();
                ConstraintViolationImpl violationImp = (ConstraintViolationImpl)violation;
                errors.append("[").append(violationImp.getPropertyPath().toString()).append(":").append(violationImp.getMessage()).append("]");
            }

            if(errors.length() > 0) {
                throw new ValidationException(String.format("[%s]对象不符合规则:%s", new Object[]{obj.getClass().getSimpleName(), errors.toString()}));
            }
        }
    }
}
