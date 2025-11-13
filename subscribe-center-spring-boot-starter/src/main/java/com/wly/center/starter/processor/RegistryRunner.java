package com.wly.center.starter.processor;

import com.wly.center.common.discovery.RegistryClient;
import com.wly.center.common.discovery.ServiceInstance;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.core.utils.NetworkUtils;
import org.springframework.boot.CommandLineRunner;

public record RegistryRunner(RegistryClient client, ExchangeServerFactory serverFactory) implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        client.register(ServiceInstance.builder().serviceName(serverFactory.registerName())
                .ip(NetworkUtils.getServerIp()).port(serverFactory.port()).build());
    }
}
