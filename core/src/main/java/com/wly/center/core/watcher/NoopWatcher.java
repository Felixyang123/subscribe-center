package com.wly.center.core.watcher;

import com.wly.center.core.enumeration.WatcherCycleEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopWatcher implements Watcher {

    @Override
    public void nodeDeleted(String nodeName) {
        log.info("NoopWatcher node deleted: {}", nodeName);
    }

    @Override
    public void nodeDataChanged(String nodeName) {
        log.info("NoopWatcher node data changed: {}", nodeName);
    }

    @Override
    public WatcherCycleEnum cycleType() {
        return WatcherCycleEnum.CYCLE;
    }

    @Override
    public String key() {
        return "";
    }
}
