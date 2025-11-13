package com.wly.center.starter.config;

import com.wly.center.core.factory.ExchangeServerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SubscribeCenterProps.class)
public class SubscribeCenterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExchangeServerFactory exchangeServerFactory(SubscribeCenterProps props) {
        return ExchangeServerFactory.builder()
                .port(props.getPort())
                .registerName(props.getRegisterName())
                .defaultBaseUrl(props.getBaseAdminUrl())
                .renewIntervalSeconds(props.getRenewIntervalSeconds())
                .build();
    }

    @Bean
    public ExchangeServerBootstrap exchangeServerBootstrap(ExchangeServerFactory exchangeServerFactory) {
        return new ExchangeServerBootstrap(exchangeServerFactory);
    }
}
