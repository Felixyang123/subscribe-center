package com.wly.center.common.discovery;

import com.wly.center.core.factory.ExchangeServerFactory;

public record RegistryClient(ExchangeServerFactory serverFactory) {

    public void register(ServiceInstance instance) {
        serverFactory.defaultNodeOptHelper().createTemporarySlaveNode(instance.getServiceName(), instance,
                serverFactory.getExpireTimestamp());
    }
}
