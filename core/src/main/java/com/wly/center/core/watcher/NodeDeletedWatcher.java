package com.wly.center.core.watcher;

import com.wly.center.core.helper.RenewNodeHelper;
import com.wly.center.core.helper.RestExchangeClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record NodeDeletedWatcher(RenewNodeHelper renewNodeHelper, RestExchangeClient client) implements Watcher{

    @Override
    public void nodeDeleted(String nodeName) {
        log.debug("NodeDeletedWatcher node deleted: {}", nodeName);

        renewNodeHelper.removeRenewNodeTask(nodeName);
    }

    @Override
    public void nodeDataChanged(String nodeName) {
        log.debug("NodeDeletedWatcher node data changed no op: {}", nodeName);
    }
}
