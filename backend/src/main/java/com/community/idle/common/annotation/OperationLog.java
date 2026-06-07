package com.community.idle.common.annotation;

import com.community.idle.common.enums.OperationType;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    OperationType type();

    String name() default "";

    String targetType() default "";

    String targetIdParam() default "id";

    String targetNameParam() default "";

    boolean recordData() default true;

    boolean requireConfirm() default false;

    int confirmThreshold() default 0;

    String confirmThresholdParam() default "";
}
