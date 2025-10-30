package com.wly.center.core.watcher;

import com.wly.center.core.enumeration.WatcherCycleEnum;

import java.util.UUID;

public interface Watcher {

    void nodeDeleted(String nodeName);

    void nodeDataChanged(String nodeName);

    default WatcherCycleEnum cycleType() {
        return WatcherCycleEnum.CYCLE;
    }

    default String key() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
