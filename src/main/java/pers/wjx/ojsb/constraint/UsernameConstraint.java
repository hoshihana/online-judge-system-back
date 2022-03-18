package pers.wjx.ojsb.constraint;

import org.hibernate.validator.constraints.Length;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({ElementType.PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = "^\\w+$", message = "用户名只能含有数字、字母或下划线")
@Length(min = 6, max = 16, message = "用户名长度必须在6到16位之间")
@NotBlank(message = "用户名不能为空")
public @interface UsernameConstraint {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
