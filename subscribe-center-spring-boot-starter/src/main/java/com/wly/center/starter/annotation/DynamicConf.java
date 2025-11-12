package com.wly.center.starter.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DynamicConf {

    String key();

    boolean refresh() default false;
}
