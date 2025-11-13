package com.wly.center.core.watcher;

import com.wly.center.core.enumeration.WatcherCycleEnum;

public interface CycleWatcher extends Watcher {
    @Override
    default WatcherCycleEnum cycleType() {
        return WatcherCycleEnum.CYCLE;
    }
}
