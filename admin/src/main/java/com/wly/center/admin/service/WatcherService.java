package com.wly.center.admin.service;

import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.dao.entity.Watcher;
import com.wly.center.admin.lock.LockTemplate;
import com.wly.center.admin.manager.NodeManager;
import com.wly.center.core.exception.NodeNotExistException;
import com.wly.center.core.pojo.req.OpenWatchReq;
import org.springframework.stereotype.Service;

@Service
public record WatcherService(NodeManager nodeManager, LockTemplate lockTemplate) {

    public void watch(OpenWatchReq req) {
        lockTemplate.lockThenExecute(req.getNodeName(), () -> {
            Node node = nodeManager.nodeStorage().get(req.getNodeName());
            if (node == null) {
                throw new NodeNotExistException("节点不存在：" + req.getNodeName());
            }

            nodeManager.watcherManager().watcherStorage().add(Watcher.builder()
                    .key(req.getKey())
                    .node(node.getName())
                    .ip(req.getIp())
                    .port(req.getPort())
                    .cycleType(req.getCycleType())
                    .notifyType(req.getNotifyType())
                    .build());
        });
    }
}
