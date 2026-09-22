package com.cham.annotation;

import com.cham.validation.EnumValidValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValidValidator.class)
public @interface EnumValid {
    
    String message() default "유효하지 않은 값입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /** 허용할 enum 클래스 */
    Class<? extends Enum<?>> enumClass();
    
    /** null 허용 여부 */
    boolean allowNull() default false;
}
