package com.cham.validation;

import com.cham.annotation.EnumValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValidValidator implements ConstraintValidator<EnumValid, Object> {
    
    private Set<String> acceptedValues;
    private boolean allowNull;
    
    @Override
    public void initialize(EnumValid constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
        acceptedValues = Arrays.stream(constraintAnnotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
    
    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        if (o == null) {
            return  allowNull;
        }
        return acceptedValues.contains(o.toString());
    }
}
