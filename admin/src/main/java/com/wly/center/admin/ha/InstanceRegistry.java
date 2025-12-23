package com.wly.center.admin.ha;

import com.wly.center.admin.dao.entity.HaServiceInstance;
import com.wly.center.admin.dao.rep.HaServiceInstanceRep;
import com.wly.center.admin.enumeration.HaInstanceStatusEnum;
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
        long cur = System.currentTimeMillis();
        HaServiceInstance instance = HaServiceInstance.builder()
                .host(NetworkUtils.getServerIp())
                .port(port)
                .serveAsMaster(Boolean.FALSE)
                .serviceName(appName)
                .expireAt(cur + renewIntervalSeconds * 1000 * 3)
                .lastHeartbeat(cur)
                .instanceStatus(HaInstanceStatusEnum.ONLINE.getCode())
                .build();
        instanceRep.getBaseMapper().upsert(instance);

        // 开启定时续租
        renewExecutor.scheduleAtFixedRate(() -> {
                    long heartbeat = System.currentTimeMillis();
                    instanceRep.updateById(HaServiceInstance.builder()
                            .id(instance.getId())
                            .expireAt(heartbeat + renewIntervalSeconds * 1000 * 3)
                            .lastHeartbeat(heartbeat)
                            .instanceStatus(HaInstanceStatusEnum.ONLINE.getCode())
                            .build());
                }, renewIntervalSeconds, renewIntervalSeconds, TimeUnit.SECONDS
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
