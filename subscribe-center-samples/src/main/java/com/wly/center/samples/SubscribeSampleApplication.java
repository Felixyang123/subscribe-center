package com.wly.center.samples;

import com.wly.center.starter.annotation.EnableConf;
import com.wly.center.starter.annotation.EnableDiscovery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDiscovery
@EnableConf
public class SubscribeSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscribeSampleApplication.class, args);
    }
}
