package com.wly.center.core.helper;

import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.exception.BusinessExceptions;
import com.wly.center.core.pojo.Result;
import com.wly.center.core.pojo.resp.OpenHaClusterInfoResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

@Slf4j
public class ClusterClient {

    private static final String HTTP_PREFIX = "http://";

    private final String adminServiceName;

    private volatile String masterAddress;

    private volatile List<String> slaveAddresses;

    private final ScheduledExecutorService refreshExecutor;

    private final ScheduledExecutorService healthCheckExecutor;

    private final RestClientHelper restClientHelper;

    public ClusterClient(String adminServiceName, List<String> discoveryAddresses, Integer refreshIntervalSeconds, Integer healthCheckIntervalSeconds) {
        this.adminServiceName = adminServiceName;
        this.refreshExecutor = Executors.newSingleThreadScheduledExecutor();
        this.healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        this.restClientHelper = RestClientHelper.builder().build();

        discovery(discoveryAddresses);

        this.refreshExecutor.scheduleAtFixedRate(this::refreshCluster, refreshIntervalSeconds, refreshIntervalSeconds, TimeUnit.SECONDS);

        this.healthCheckExecutor.scheduleAtFixedRate(this::healthCheck, 0, healthCheckIntervalSeconds, TimeUnit.SECONDS);
    }

    public void destroy() {
        this.refreshExecutor.shutdown();
        this.healthCheckExecutor.shutdown();
    }

    private <T> T extractData(Result<T> result) {
        if (Boolean.TRUE.equals(result.getSuccess())) {
            return result.getData();
        }
        throw new BusinessException(result.getCode(), result.getMessage());
    }

    private void refreshCluster() {
        if (StringUtils.hasText(this.masterAddress)) {
            OpenHaClusterInfoResp clusterInfoResp;
            try {
                RestClientHelper helper = RestClientHelper.builder().baseUrl(this.masterAddress).build();
                Result<OpenHaClusterInfoResp> result = helper.get("/open/ha/cluster",
                        new ParameterizedTypeReference<>() {
                        },
                        MultiValueMap.fromSingleValue(Map.of("serviceName", adminServiceName)));
                clusterInfoResp = extractData(result);
            } catch (Exception e) {
                log.warn("refresh cluster fail, begin discovery", e);
                discovery(this.slaveAddresses);
                return;
            }

            if (clusterInfoResp.getMaster() == null) {
                log.warn("master instance not exists, begin discovery");
                discovery(this.slaveAddresses);
                return;
            }

            this.masterAddress = HTTP_PREFIX + clusterInfoResp.getMaster().getHost() + ":" + clusterInfoResp.getMaster().getPort();
            if (!CollectionUtils.isEmpty(clusterInfoResp.getSalves())) {
                this.slaveAddresses = clusterInfoResp.getSalves().stream().map(salve -> HTTP_PREFIX + salve.getHost() + ":" + salve.getPort()).toList();
            }
        }
    }

    private void healthCheck() {
        if (!StringUtils.hasText(this.masterAddress)) {
            throw new BusinessException(BusinessExceptions.MASTER_NOT_EXIST.name(), "healthcheck fail, masterAddress is empty");
        }

        try {
            RestClientHelper helper = RestClientHelper.builder().baseUrl(this.masterAddress).build();
            helper.get("/open/ha/probe", String.class);
        } catch (Exception e) {
            log.warn("health check fail, begin discovery", e);
            discovery(this.slaveAddresses);
        }
    }

    private void discovery(List<String> discoveryAddresses) {
        if (CollectionUtils.isEmpty(discoveryAddresses)) {
            throw new BusinessException(BusinessExceptions.DISCOVERY_ADDRESS_NOT_EXIST.name(), "discoveryAddresses is empty");
        }

        for (String discoveryAddress : discoveryAddresses) {
            try {
                RestClientHelper helper = RestClientHelper.builder().baseUrl(discoveryAddress).build();
                Result<OpenHaClusterInfoResp> result = helper.get("/open/ha/cluster",
                        new ParameterizedTypeReference<>() {
                        },
                        MultiValueMap.fromSingleValue(Map.of("serviceName", adminServiceName)));
                OpenHaClusterInfoResp clusterInfoResp = extractData(result);

                if (clusterInfoResp.getMaster() == null) {
                    log.warn("discovery master address empty: {}", discoveryAddress);
                    continue;
                }

                this.masterAddress = HTTP_PREFIX + clusterInfoResp.getMaster().getHost() + ":" + clusterInfoResp.getMaster().getPort();

                if (!CollectionUtils.isEmpty(clusterInfoResp.getSalves())) {
                    this.slaveAddresses = clusterInfoResp.getSalves().stream().map(salve -> HTTP_PREFIX + salve.getHost() + ":" + salve.getPort()).toList();
                }
                break;
            } catch (Exception e) {
                log.warn("discovery address fail: {}", discoveryAddress, e);
            }
        }
    }

    public <T> T execute(String path, BiFunction<String, RestClientHelper, T> processor) {
        String url = this.masterAddress + path;
        AtomicInteger retryCount = new AtomicInteger(0);
        return execAndRetry(processor, url, retryCount);
    }

    private <T> T execAndRetry(BiFunction<String, RestClientHelper, T> processor, String url, AtomicInteger retryCount) {
        try {
            return processor.apply(url, restClientHelper);
        } catch (Exception e) {
            log.warn("execute fail, begin discovery", e);
//            discovery(this.slaveAddresses);

            int count = retryCount.incrementAndGet();
            if (count > 3) {
                log.warn("execute fail, exceed max retry count");
                throw e;
            }
            log.debug("retry execute: {}", count);
            return execAndRetry(processor, url, retryCount);
        }
    }
}
