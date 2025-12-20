package com.wly.center.admin.selector;

import com.wly.center.admin.dao.entity.HaServiceInstance;
import com.wly.center.admin.dao.rep.HaServiceInstanceRep;
import com.wly.center.core.utils.NetworkUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class InstanceRegistry implements SmartLifecycle {

    private ScheduledExecutorService renewExecutor = Executors.newSingleThreadScheduledExecutor();

    private final HaServiceInstanceRep instanceRep;


    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.port}")
    private Integer port;

    @Value("${master.ha.select.renewInterval:5}")
    private Long renewIntervalSeconds;

    @Override
    public void start() {
        // 保存实例
        HaServiceInstance instance = HaServiceInstance.builder()
                .host(NetworkUtils.getServerIp())
                .port(port)
                .serveAsMaster(Boolean.FALSE)
                .serviceName(appName)
                .expireAt(System.currentTimeMillis() + renewIntervalSeconds * 1000 * 3)
                .build();
        instanceRep.save(instance);

        // 开启定时续租
        renewExecutor.scheduleAtFixedRate(() ->
                        instanceRep.updateById(HaServiceInstance.builder()
                                .id(instance.getId())
                                .expireAt(System.currentTimeMillis() + renewIntervalSeconds * 1000 * 3)
                                .build()),
                renewIntervalSeconds,
                renewIntervalSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void stop() {
        renewExecutor.shutdownNow();
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
