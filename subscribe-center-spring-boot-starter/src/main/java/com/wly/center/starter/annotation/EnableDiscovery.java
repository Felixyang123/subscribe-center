package com.wly.center.starter.annotation;

import com.wly.center.starter.config.DiscoveryCenterAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EnableSubscribeCenter
@Import(DiscoveryCenterAutoConfiguration.class)
public @interface EnableDiscovery {
}
