package com.wly.center.starter.config;

import com.wly.center.core.factory.ExchangeServerFactory;
import org.springframework.context.SmartLifecycle;

public record ExchangeServerBootstrap(ExchangeServerFactory serverFactory) implements SmartLifecycle {

    @Override
    public void start() {
        serverFactory.start();
    }

    @Override
    public void stop() {
        serverFactory.stop();
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
