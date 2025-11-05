package com.wly.center.core.watcher;

import com.wly.center.core.enumeration.WatcherCycleEnum;
import com.wly.center.core.helper.RenewNodeHelper;
import com.wly.center.core.helper.RestExchangeClient;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public record RenewNodeTaskWatcher(RenewNodeHelper renewNodeHelper, RestExchangeClient client,
                                   String renewTaskKey, String key) implements Watcher {

    public RenewNodeTaskWatcher(RenewNodeHelper renewNodeHelper, RestExchangeClient client, String renewTaskKey) {
        this(renewNodeHelper, client, renewTaskKey, UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public void nodeDeleted(String nodeName) {
        log.debug("RenewNodeTaskWatcher node deleted: {}", nodeName);

        renewNodeHelper.removeTaskByKey(renewTaskKey);
    }

    @Override
    public void nodeDataChanged(String nodeName) {
        log.debug("RenewNodeTaskWatcher node data changed no op: {}", nodeName);
    }

    @Override
    public WatcherCycleEnum cycleType() {
        return WatcherCycleEnum.SINGLE;
    }

    @Override
    public String key() {
        return key;
    }
}
