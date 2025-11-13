package com.wly.center.starter.config;

import com.wly.center.common.conf.ConfClient;
import com.wly.center.common.discovery.DiscoveryClient;
import com.wly.center.common.discovery.RegistryClient;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.starter.processor.DynamicConfAnnotationProcessor;
import com.wly.center.starter.processor.RegistryRunner;
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

    @Bean
    public ConfClient confClient(ExchangeServerFactory exchangeServerFactory) {
        return new ConfClient(exchangeServerFactory.watchClient());
    }

    @Bean
    public DynamicConfAnnotationProcessor dynamicConfAnnotationProcessor(ConfClient confClient) {
        return new DynamicConfAnnotationProcessor(confClient);
    }

    @Bean
    public DiscoveryClient discoveryClient(ExchangeServerFactory exchangeServerFactory) {
        return new DiscoveryClient(exchangeServerFactory);
    }

    @Bean
    public RegistryClient registryClient(ExchangeServerFactory exchangeServerFactory) {
        return new RegistryClient(exchangeServerFactory);
    }

    @Bean
    public RegistryRunner registryRunner(RegistryClient client, ExchangeServerFactory serverFactory) {
        return new RegistryRunner(client, serverFactory);
    }
}
