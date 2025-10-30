package com.wly.center.core.watcher;

import com.alibaba.fastjson2.JSON;
import com.wly.center.core.helper.RestExchangeClient;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DataChangedWatcher<T> implements Watcher {

    private final RestExchangeClient client;

    private final Class<T> clazz;

    public DataChangedWatcher(RestExchangeClient client, Class<T> clazz) {
        this.client = client;
        this.clazz = clazz;
    }

    @Override
    public void nodeDeleted(String nodeName) {
        log.debug("DataChangedWatcher node deleted no op: {}", nodeName);
    }

    @Override
    public void nodeDataChanged(String nodeName) {
        OpenNodeDetailResp detail = client.getNodeDetail(nodeName);
        if (detail != null) {
            T data = JSON.parseObject(detail.getData(), this.clazz);

            log.debug("node data changed: nodeName={}, data={}", nodeName, data);
            handle(nodeName, data);
        }
    }

    abstract public void handle(String nodeName, T data);
}
