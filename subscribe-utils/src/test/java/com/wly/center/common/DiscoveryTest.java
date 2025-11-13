package com.wly.center.common;

import com.wly.center.common.discovery.DiscoveryClient;
import com.wly.center.common.discovery.RegistryClient;
import com.wly.center.common.discovery.ServiceInstance;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.core.utils.NetworkUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DiscoveryTest {

    @Test
    void discoverTest() {
        ExchangeServerFactory factory = ExchangeServerFactory.builder()
                .port(8300)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(60L)
                .build();
        factory.start();
        RegistryClient registryClient = new RegistryClient(factory);

        ExchangeServerFactory factory1 = ExchangeServerFactory.builder()
                .port(8301)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(60L)
                .build();
        factory1.start();
        DiscoveryClient discoveryClient = new DiscoveryClient(factory1);

        ServiceInstance newInstance = ServiceInstance.builder().serviceName("discovery01").ip(NetworkUtils.getServerIp())
                .port(factory.port()).build();
        registryClient.register(newInstance);

        List<ServiceInstance> instances = discoveryClient.instances("discovery01");
        Assertions.assertEquals(1, instances.size());
        ServiceInstance instance = instances.getFirst();
        Assertions.assertEquals(instance, newInstance);

        factory.stop();
        factory1.stop();
    }

    @Test
    @SneakyThrows
    void InstanceDownTest() {
        ExchangeServerFactory factory = ExchangeServerFactory.builder()
                .port(8300)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(1L)
                .build();
        factory.start();
        RegistryClient registryClient = new RegistryClient(factory);

        ExchangeServerFactory factory2 = ExchangeServerFactory.builder()
                .port(8302)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(1L)
                .build();
        factory2.start();
        RegistryClient registryClient2 = new RegistryClient(factory2);

        ExchangeServerFactory factory1 = ExchangeServerFactory.builder()
                .port(8301)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(60L)
                .build();
        factory1.start();
        DiscoveryClient discoveryClient = new DiscoveryClient(factory1);

        ServiceInstance newInstance = ServiceInstance.builder().serviceName("discovery01").ip(NetworkUtils.getServerIp())
                .port(factory.port()).build();
        registryClient.register(newInstance);

        ServiceInstance newInstance2 = ServiceInstance.builder().serviceName("discovery01").ip(NetworkUtils.getServerIp())
                .port(factory2.port()).build();
        registryClient2.register(newInstance2);

        List<ServiceInstance> instances = discoveryClient.instances("discovery01");
        Assertions.assertEquals(2, instances.size());

        Map<Integer, ServiceInstance> instanceMap = instances.stream().collect(Collectors.toMap(ServiceInstance::getPort, Function.identity()));
        Assertions.assertEquals(newInstance, instanceMap.get(factory.port()));
        Assertions.assertEquals(newInstance2, instanceMap.get(factory2.port()));

        factory2.stop();
        Thread.sleep(4000);

        instances = discoveryClient.instances("discovery01");
        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals(newInstance, instances.getFirst());
        factory.stop();
        factory1.stop();
    }

    @Test
    @SneakyThrows
    void InstanceUpTest() {
        ExchangeServerFactory factory = ExchangeServerFactory.builder()
                .port(8300)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(1L)
                .build();
        factory.start();
        RegistryClient registryClient = new RegistryClient(factory);

        ExchangeServerFactory factory1 = ExchangeServerFactory.builder()
                .port(8301)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(60L)
                .build();
        factory1.start();
        DiscoveryClient discoveryClient = new DiscoveryClient(factory1);

        ServiceInstance newInstance = ServiceInstance.builder().serviceName("discovery01").ip(NetworkUtils.getServerIp())
                .port(factory.port()).build();
        registryClient.register(newInstance);

        List<ServiceInstance> instances = discoveryClient.instances("discovery01");
        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals(newInstance, instances.getFirst());

        ExchangeServerFactory factory2 = ExchangeServerFactory.builder()
                .port(8302)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(1L)
                .build();
        factory2.start();
        RegistryClient registryClient2 = new RegistryClient(factory2);

        ServiceInstance newInstance2 = ServiceInstance.builder().serviceName("discovery01").ip(NetworkUtils.getServerIp())
                .port(factory2.port()).build();
        registryClient2.register(newInstance2);

        Thread.sleep(1000);

        instances = discoveryClient.instances("discovery01");
        Map<Integer, ServiceInstance> instanceMap = instances.stream().collect(Collectors.toMap(ServiceInstance::getPort, Function.identity()));
        Assertions.assertEquals(newInstance, instanceMap.get(factory.port()));
        Assertions.assertEquals(newInstance2, instanceMap.get(factory2.port()));

        factory2.stop();
        factory.stop();
        factory1.stop();
    }
}
