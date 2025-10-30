package com.wly.center.starter.annotation;

import com.wly.center.starter.config.SubscribeCenterAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(SubscribeCenterAutoConfiguration.class)
public @interface EnableSubscribeCenter {
}
