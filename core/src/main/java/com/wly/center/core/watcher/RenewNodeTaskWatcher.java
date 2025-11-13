package com.wly.center.core.watcher;

import com.wly.center.core.helper.RenewNodeHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public record RenewNodeTaskWatcher(RenewNodeHelper renewNodeHelper, String renewTaskKey,
                                   String key) implements NodeDeletedWatcher {

    public RenewNodeTaskWatcher(RenewNodeHelper renewNodeHelper, String renewTaskKey) {
        this(renewNodeHelper, renewTaskKey, UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public void nodeDeleted(String node) {
        log.debug("RenewNodeTaskWatcher node deleted: {}", node);

        renewNodeHelper.removeTaskByKey(renewTaskKey);
    }

    @Override
    public String key() {
        return key;
    }
}
