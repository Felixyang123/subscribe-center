package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.dao.entity.Watcher;
import com.wly.center.core.client.ExchangeClient;
import com.wly.center.core.enumeration.ExchangeType;
import com.wly.center.core.enumeration.WatcherCycleEnum;
import com.wly.center.core.enumeration.WatcherExchangeType;
import com.wly.center.core.protocal.ExchangeRequest;
import com.wly.center.core.protocal.WatcherExchangeReq;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record WatcherManager(WatcherStorage watcherStorage) {

    public void nodeDeleted(Node node) {
        List<Watcher> watchers = watcherStorage.remove(node.getId());
        if (CollectionUtils.isEmpty(watchers)) {
            return;
        }

        notify(node, watchers, WatcherExchangeType.NODE_DELETE);
    }

    public synchronized void dataChanged(Node node) {
        List<Watcher> watchers = watcherStorage.watchers(node.getId());
        if (CollectionUtils.isEmpty(watchers)) {
            return;
        }

        notify(node, watchers, WatcherExchangeType.DATA_CHANGE);

        List<Watcher> singleWatchers = watchers.stream().filter(w -> WatcherCycleEnum.SINGLE.getCode().equals(w.getCycleType())).toList();
        watcherStorage.remove(singleWatchers);
    }

    private void notify(Node node, List<Watcher> watchers, WatcherExchangeType exchangeType) {
        Map<String, List<Watcher>> watcherPartitionMap = watchers.stream().collect(Collectors.groupingBy(watcher -> watcher.getIp() + ":" + watcher.getPort()));
        for (List<Watcher> partition : watcherPartitionMap.values()) {
            Watcher watcher = partition.getFirst();
            String requestId = UUID.randomUUID().toString().replace("-", "");

            ExchangeClient.send(ExchangeRequest.builder()
                    .requestId(requestId)
                    .ip(watcher.getIp())
                    .port(watcher.getPort())
                    .executionTime(System.currentTimeMillis())
                    .type(ExchangeType.WATCHER.getCode())
                    .req(WatcherExchangeReq.builder()
                            .exchangeType(exchangeType.getCode())
                            .node(node.getName())
                            .build())
                    .build());
        }
    }
}
