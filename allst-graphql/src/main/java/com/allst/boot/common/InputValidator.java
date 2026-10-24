package com.allst.boot.common;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InputValidator {

    private final Validator validator;

    public InputValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(Object target) {
        if (target == null) {
            throw ApiException.invalid("入参不能为空");
        }
        Set<ConstraintViolation<Object>> violations = validator.validate(target);
        if (violations.isEmpty()) {
            return;
        }
        String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + violation.getMessage())
                .collect(Collectors.joining("；"));
        throw ApiException.invalid(message);
    }
}
