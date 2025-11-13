package com.wly.center.core.watcher;

import com.wly.center.core.enumeration.WatcherCycleEnum;
import com.wly.center.core.enumeration.WatcherExchangeType;

public interface Watcher {

    void notify(String node, Integer exchangeType);

    WatcherCycleEnum cycleType();

    String key();

    WatcherExchangeType exchangeType();
}
