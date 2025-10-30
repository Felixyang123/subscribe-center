package com.wly.center.core.watcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopWatcher implements Watcher{

    @Override
    public void nodeDeleted(String nodeName) {
        log.info("NoopWatcher node deleted: {}", nodeName);
    }

    @Override
    public void nodeDataChanged(String nodeName) {
        log.info("NoopWatcher node data changed: {}", nodeName);
    }
}
