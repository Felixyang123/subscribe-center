package com.wly.center.common.discovery;

import com.alibaba.fastjson2.JSON;
import com.wly.center.core.enumeration.WatcherExchangeType;
import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.exception.BusinessExceptions;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import com.wly.center.core.watcher.CycleWatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RequiredArgsConstructor
public class DiscoveryClient {

    private final ConcurrentMap<String, List<ServiceInstance>> instancesMap = new ConcurrentHashMap<>();

    private final ExchangeServerFactory exchangeServerFactory;

    public List<ServiceInstance> instances(String serviceName) {
        return instancesMap.computeIfAbsent(serviceName, k -> {
            try {
                // query
                List<OpenNodeDetailResp> children = exchangeServerFactory.defaultExchangeClient().children(serviceName);
                // watch
                exchangeServerFactory.watchClient().watch(serviceName, new DiscoveryServiceWatcher(this));
                // transfer and return
                return children.stream().filter(child -> StringUtils.hasText(child.getData()))
                        .map(child -> JSON.parseObject(child.getData(), ServiceInstance.class)).toList();
            } catch (Exception e) {
                if (e instanceof BusinessException businessException && BusinessExceptions.NODE_NOT_EXIST.name()
                        .equals(businessException.getCode())) {
                    return new ArrayList<>();
                }

                log.warn("query service instances failed, serviceName: {}, error: ", serviceName, e);
                throw new BusinessException(BusinessExceptions.DEFAULT_ERROR.name(), e.getMessage());
            }
        });
    }

    public void refresh(String serviceName) {
        instancesMap.compute(serviceName, (k, v) -> {
            try {
                List<OpenNodeDetailResp> children = exchangeServerFactory.defaultExchangeClient().children(serviceName);

                return children.stream().filter(child -> StringUtils.hasText(child.getData()))
                        .map(child -> JSON.parseObject(child.getData(), ServiceInstance.class)).toList();
            } catch (Exception e) {
                if (e instanceof BusinessException businessException && BusinessExceptions.NODE_NOT_EXIST.name()
                        .equals(businessException.getCode())) {
                    return new ArrayList<>();
                }

                log.warn("query service instances failed, serviceName: {}, error: ", serviceName, e);
                return v;
            }
        });
    }

    public record DiscoveryServiceWatcher(DiscoveryClient client, String key) implements CycleWatcher {

        public DiscoveryServiceWatcher(DiscoveryClient client) {
            this(client, UUID.randomUUID().toString().replace("-", ""));
        }

        @Override
        public void notify(String node, Integer exchangeType) {
            if (exchangeType().getCode().equals(exchangeType)) {
                log.debug("discovery service node data changed, node: {}", node);

                client.refresh(node);
            }
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public WatcherExchangeType exchangeType() {
            return WatcherExchangeType.CHILDREN_LIST_CHANGE;
        }
    }
}
