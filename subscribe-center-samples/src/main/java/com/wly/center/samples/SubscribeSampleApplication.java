package com.wly.center.samples;

import com.wly.center.starter.annotation.EnableSubscribeCenter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSubscribeCenter
public class SubscribeSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscribeSampleApplication.class, args);
    }
}
