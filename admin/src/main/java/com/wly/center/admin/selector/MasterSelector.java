package com.wly.center.admin.selector;

import com.wly.center.admin.pojo.req.HaMasterSelectReq;
import com.wly.center.admin.service.HaMasterSelectService;
import com.wly.center.core.utils.NetworkUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterSelector implements CommandLineRunner {

    private final HaMasterSelectService selectService;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.port}")
    private Integer port;

    @Value("${master.ha.select.renewInterval:5}")
    private Long renewIntervalSeconds;

    @Value("${master.ha.select.enable:false}")
    private Boolean enableMasterSelect;

    @Override
    public void run(String... args) throws Exception {
        if (enableMasterSelect) {
            HaMasterSelectReq masterSelectReq = HaMasterSelectReq.builder()
                    .serviceName(appName)
                    .host(NetworkUtils.getServerIp())
                    .port(port)
                    .renewIntervalSeconds(renewIntervalSeconds)
                    .build();
            log.info("Starting HA master selection, req: {}", masterSelectReq);
            Future<?> future = selectService.asyncServeAsMaster(masterSelectReq);
            try {
                future.get();
            } catch (Exception e) {
                log.error("Start HA master selection fail, req:{}, error ", masterSelectReq, e);
            }
        }
    }
}
