package com.wly.center.core.watcher;

import com.wly.center.core.enumeration.WatcherExchangeType;

public interface NodeDeletedWatcher extends SingleWatcher {

    void nodeDeleted(String nodeName);

    @Override
    default void notify(String node, Integer exchangeType) {
        if (exchangeType().getCode().equals(exchangeType)) {
            nodeDeleted(node);
        }
    }


    @Override
    default WatcherExchangeType exchangeType() {
        return WatcherExchangeType.NODE_DELETE;
    }
}
