package com.wly.center.common.ha;

import com.wly.center.core.enumeration.WatcherCycleEnum;
import com.wly.center.core.watcher.Watcher;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public record HaWatcher(HaSelector selector, String key) implements Watcher {
    public HaWatcher(HaSelector selector) {
        this(selector, UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public void nodeDeleted(String nodeName) {
        log.debug("Master node deleted: {}", nodeName);

        selector.select(nodeName);
    }

    @Override
    public void nodeDataChanged(String nodeName) {
    }

    @Override
    public WatcherCycleEnum cycleType() {
        return WatcherCycleEnum.SINGLE;
    }

    @Override
    public String key() {
        return this.key;
    }
}
