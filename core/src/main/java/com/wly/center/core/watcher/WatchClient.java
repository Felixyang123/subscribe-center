package com.wly.center.core.watcher;

import com.wly.center.core.enumeration.WatcherCycleEnum;
import com.wly.center.core.enumeration.WatcherExchangeType;
import com.wly.center.core.helper.RestExchangeClient;
import com.wly.center.core.pojo.req.OpenWatchReq;
import com.wly.center.core.protocal.WatcherExchangeReq;
import com.wly.center.core.utils.NetworkUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public record WatchClient(RestExchangeClient restExchangeClient, int port) {

    public void watch(String nodeName, Watcher watcher, WatcherCycleEnum cycleType) {
        restExchangeClient.watchNode(
                OpenWatchReq.builder()
                        .nodeName(nodeName)
                        .ip(NetworkUtils.getServerIp())
                        .port(port)
                        .cycleType(cycleType.getCode())
                        .build());

        WatcherManager.getInstance().addWatcher(nodeName, watcher);
    }

    public void watch(String nodeName, Watcher watcher) {
        watch(nodeName, watcher, WatcherCycleEnum.CYCLE);
    }

    public void notify(WatcherExchangeReq req) {
        List<Watcher> watchers = WatcherManager.getInstance().getWatchers(req.getNode());
        if (CollectionUtils.isEmpty(watchers)) {
            return;
        }

        if (WatcherExchangeType.NODE_DELETE.getCode().equals(req.getExchangeType())) {
            watchers.forEach(watcher -> watcher.nodeDeleted(req.getNode()));
        } else if (WatcherExchangeType.DATA_CHANGE.getCode().equals(req.getExchangeType())) {
            watchers.forEach(watcher -> watcher.nodeDataChanged(req.getNode()));
        }

        List<Watcher> singleWatchers = watchers.stream().filter(watcher -> watcher.cycleType().equals(WatcherCycleEnum.SINGLE)).toList();
        WatcherManager.getInstance().removeWatchers(req.getNode(), singleWatchers);
    }

}
