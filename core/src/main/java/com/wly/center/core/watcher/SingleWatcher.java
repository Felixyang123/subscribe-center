package com.wly.center.core.watcher;

import com.wly.center.core.enumeration.WatcherCycleEnum;

public interface SingleWatcher extends Watcher{
    @Override
    default WatcherCycleEnum cycleType() {
        return WatcherCycleEnum.SINGLE;
    }
}
