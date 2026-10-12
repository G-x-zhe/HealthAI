package com.healthai.common;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)   // 运行时可通过反射读取
public @interface OperationLog {
    String module() default "";       // 模块名
    String operation() default "";    // 操作描述
}