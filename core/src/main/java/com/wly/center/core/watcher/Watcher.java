package com.wly.center.core.watcher;

import com.wly.center.core.enumeration.WatcherCycleEnum;

public interface Watcher {

    void nodeDeleted(String nodeName);

    void nodeDataChanged(String nodeName);

    WatcherCycleEnum cycleType();

    String key();
}
