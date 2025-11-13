package com.wly.center.starter.config;

import com.wly.center.common.conf.ConfClient;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.starter.processor.DynamicConfAnnotationProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SubscribeCenterAutoConfiguration.class)
public class ConfCenterAutoConfiguration {

    @Bean
    public ConfClient confClient(ExchangeServerFactory exchangeServerFactory) {
        return new ConfClient(exchangeServerFactory.watchClient());
    }

    @Bean
    public DynamicConfAnnotationProcessor dynamicConfAnnotationProcessor(ConfClient confClient) {
        return new DynamicConfAnnotationProcessor(confClient);
    }
}
