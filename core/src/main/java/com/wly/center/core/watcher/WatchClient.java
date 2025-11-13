package com.wly.center.core.watcher;

import com.wly.center.core.enumeration.WatcherCycleEnum;
import com.wly.center.core.helper.RestExchangeClient;
import com.wly.center.core.pojo.req.OpenWatchReq;
import com.wly.center.core.protocal.WatcherExchangeReq;
import com.wly.center.core.utils.NetworkUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public record WatchClient(RestExchangeClient restExchangeClient, LocalWatcherManager watcherManager, int port) {

    public void watch(String nodeName, Watcher watcher) {
        restExchangeClient.watchNode(
                OpenWatchReq.builder()
                        .key(watcher.key())
                        .nodeName(nodeName)
                        .ip(NetworkUtils.getServerIp())
                        .port(port)
                        .cycleType(watcher.cycleType().getCode())
                        .notifyType(watcher.exchangeType().getCode())
                        .build());

        watcherManager.addWatcher(nodeName, watcher);
    }

    public synchronized void notify(WatcherExchangeReq req) {
        List<Watcher> watchers = watcherManager.getWatchers(req.getNode());

        if (!CollectionUtils.isEmpty(req.getKeys())) {
            watchers = watchers.stream().filter(watcher -> req.getKeys().contains(watcher.key())).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(watchers)) {
            return;
        }

        watchers.forEach(watcher -> watcher.notify(req.getNode(), req.getExchangeType()));

        List<Watcher> singleWatchers = watchers.stream().filter(watcher -> watcher.cycleType().equals(WatcherCycleEnum.SINGLE)).toList();
        watcherManager.removeWatchers(req.getNode(), singleWatchers);
    }

}
