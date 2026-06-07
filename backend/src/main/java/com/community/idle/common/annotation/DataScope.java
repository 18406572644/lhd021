package com.community.idle.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    String businessType() default "";

    String tableAlias() default "";

    String columnName() default "";

    boolean userScope() default false;

    String userColumnName() default "user_id";
}
