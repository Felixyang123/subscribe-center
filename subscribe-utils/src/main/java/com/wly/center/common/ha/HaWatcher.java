package com.wly.center.common.ha;

import com.wly.center.core.watcher.NodeDeletedWatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public record HaWatcher(HaSelector selector, String key) implements NodeDeletedWatcher {
    public HaWatcher(HaSelector selector) {
        this(selector, UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public void nodeDeleted(String nodeName) {
        log.debug("Master node deleted: {}", nodeName);

        selector.select(nodeName);
    }

    @Override
    public String key() {
        return this.key;
    }
}
