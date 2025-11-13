package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.dao.entity.Watcher;
import com.wly.center.admin.lock.LockTemplate;
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

public record WatcherManager(WatcherStorage watcherStorage, LockTemplate lockTemplate) {

    public void nodeDeleted(Node node) {
        List<Watcher> watchers = watcherStorage.remove(node.getName());
        if (CollectionUtils.isEmpty(watchers)) {
            return;
        }

        List<Watcher> nodeDeletedWatchers = watchers.stream().filter(w ->
                WatcherExchangeType.NODE_DELETE.getCode().equals(w.getNotifyType())).toList();

        notify(node, nodeDeletedWatchers, WatcherExchangeType.NODE_DELETE);
    }

    public void childrenListChanged(Node node) {
        notifyAndClearWatchers(node, WatcherExchangeType.CHILDREN_LIST_CHANGE);
    }

    public void dataChanged(Node node) {
        notifyAndClearWatchers(node, WatcherExchangeType.DATA_CHANGE);
    }

    public void notifyAndClearWatchers(Node node, WatcherExchangeType exchangeType) {
        lockTemplate.lockThenExecute(node.getName(), () -> {
            List<Watcher> watchers = watcherStorage.watchers(node.getName());
            if (CollectionUtils.isEmpty(watchers)) {
                return;
            }

            List<Watcher> toExecWatchers = watchers.stream().filter(w ->
                    exchangeType.getCode().equals(w.getNotifyType())).toList();

            notify(node, toExecWatchers, exchangeType);

            List<Watcher> singleWatchers = toExecWatchers.stream().filter(w ->
                    WatcherCycleEnum.SINGLE.getCode().equals(w.getCycleType())).toList();
            watcherStorage.remove(singleWatchers);
        });
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
                            .keys(partition.stream().map(Watcher::getKey).toList())
                            .exchangeType(exchangeType.getCode())
                            .node(node.getName())
                            .build())
                    .build());
        }
    }
}
