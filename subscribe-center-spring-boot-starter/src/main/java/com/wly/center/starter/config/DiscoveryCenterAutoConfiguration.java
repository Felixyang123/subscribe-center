package com.wly.center.starter.config;

import com.wly.center.common.discovery.DiscoveryClient;
import com.wly.center.common.discovery.RegistryClient;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.starter.processor.RegistryRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscoveryCenterAutoConfiguration {

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
