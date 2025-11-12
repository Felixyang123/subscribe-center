package com.wly.center.common.conf;

import com.wly.center.core.enumeration.WatcherCycleEnum;
import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.exception.BusinessExceptions;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import com.wly.center.core.watcher.WatchClient;
import com.wly.center.core.watcher.Watcher;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RequiredArgsConstructor
public class ConfClient {

    private final ConcurrentMap<String, ConfData> confDataMap = new ConcurrentHashMap<>();

    @Getter
    private final WatchClient watchClient;

    public <T> ConfData get(String key, Class<T> clz) {

        return confDataMap.computeIfAbsent(key, k -> {
            try {
                //get and watch
                OpenNodeDetailResp node = watchClient.restExchangeClient().getNodeDetail(key);

                watchClient.watch(key, new ConfClientWatcher(clz, this));

                return new ConfData(clz, node.getData());
            } catch (Exception e) {
                if (e instanceof BusinessException businessException &&
                        BusinessExceptions.NODE_NOT_EXIST.name().equals(businessException.getCode())) {
                    return null;
                }

                log.error("get conf error", e);
                throw new BusinessException(BusinessExceptions.DEFAULT_ERROR.name(), e.getMessage());
            }
        });
    }

    public <T> void refresh(String key, Class<T> clz) {
        confDataMap.compute(key, (k, v) -> {
            try {
                OpenNodeDetailResp node = watchClient.restExchangeClient().getNodeDetail(key);
                return new ConfData(clz, node.getData());
            } catch (Exception e) {
                if (e instanceof BusinessException businessException &&
                        BusinessExceptions.NODE_NOT_EXIST.name().equals(businessException.getCode())) {
                    return null;
                }

                log.error("get conf error", e);
                return v;
            }
        });
    }

    public void removeCache(String key) {
        confDataMap.remove(key);
    }

    public <T> T value(String key, Object defaultValue, Class<T> clz) {
        Object value = Optional.ofNullable(get(key, clz)).map(ConfData::getData).orElse(defaultValue);
        return (T) value;
    }

    public <T> T value(String key, Class<T> clz) {
        return value(key, null, clz);
    }

    public record ConfClientWatcher(Class<?> clz, ConfClient confClient, String key) implements Watcher {

        public ConfClientWatcher(Class<?> clz, ConfClient confClient) {
            this(clz, confClient, UUID.randomUUID().toString().replace("-", ""));
        }

        @Override
        public void nodeDeleted(String nodeName) {
            log.debug("conf data node deleted, node: {}", nodeName);
            confClient.removeCache(nodeName);
        }

        @Override
        public void nodeDataChanged(String nodeName) {
            log.debug("conf data node data changed, node: {}", nodeName);
            confClient.refresh(nodeName, clz);
        }

        @Override
        public WatcherCycleEnum cycleType() {
            return WatcherCycleEnum.CYCLE;
        }

        @Override
        public String key() {
            return key;
        }
    }
}
