package com.wly.center.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "subscribe-center")
public class SubscribeCenterProps {

    private Integer port;

    private String baseAdminUrl;

    private Long renewIntervalSeconds;

    private String registerName;

    private String adminServiceName;

    private List<String> discoveryAddresses;

    private Integer refreshClusterIntervalSeconds;

    private Integer healthCheckIntervalSeconds;
}
