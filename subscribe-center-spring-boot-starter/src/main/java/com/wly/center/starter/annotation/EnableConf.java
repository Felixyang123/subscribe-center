package com.wly.center.starter.annotation;

import com.wly.center.starter.config.ConfCenterAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ConfCenterAutoConfiguration.class)
public @interface EnableConf {
}
