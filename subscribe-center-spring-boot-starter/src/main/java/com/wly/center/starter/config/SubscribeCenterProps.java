package com.wly.center.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "subscribe-center")
public class SubscribeCenterProps {

    private Integer port;

    private String baseAdminUrl;

    private Long renewIntervalSeconds;
}
