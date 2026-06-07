package com.community.idle.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    String[] value() default {};

    String[] roles() default {};

    Logical logical() default Logical.AND;

    enum Logical {
        AND, OR
    }
}
