package com.wly.center.core.watcher;

import com.alibaba.fastjson2.JSON;
import com.wly.center.core.enumeration.WatcherExchangeType;
import com.wly.center.core.helper.RestExchangeClient;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public abstract class AbstractDataChangedWatcher<T> implements CycleWatcher {

    private final RestExchangeClient client;

    private final Class<T> clazz;

    private final String key;

    public AbstractDataChangedWatcher(RestExchangeClient client, Class<T> clazz) {
        this.client = client;
        this.clazz = clazz;
        this.key = UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public void notify(String node, Integer exchangeType) {
        if (exchangeType().getCode().equals(exchangeType)) {
            OpenNodeDetailResp detail = client.getNodeDetail(node);
            if (detail != null) {
                T data = JSON.parseObject(detail.getData(), this.clazz);

                log.debug("node data changed: nodeName={}, data={}", node, data);
                dataChanged(node, data);
            }
        }
    }

    abstract public void dataChanged(String nodeName, T data);

    @Override
    public WatcherExchangeType exchangeType() {
        return WatcherExchangeType.DATA_CHANGE;
    }

    @Override
    public String key() {
        return this.key;
    }
}
