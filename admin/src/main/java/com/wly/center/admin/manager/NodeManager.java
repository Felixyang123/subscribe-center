package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.lock.LockTemplate;
import com.wly.center.core.exception.NodeExistException;

public record NodeManager(NodeStorage nodeStorage, WatcherManager watcherManager, LockTemplate lockTemplate) {

    public void add(Node node) {
        lockTemplate.lockThenExecute(node.getName(), () -> {
            if (!nodeStorage.add(node)) {
                throw new NodeExistException("节点已存在：" + node.getName());
            }
        });
    }

    public void remove(Long nodeId) {
        Node node = nodeStorage.remove(nodeId);

        if (node != null) {
            afterRemove(node);
        }
    }

    public void afterRemove(Node node) {
        watcherManager.nodeDeleted(node);

        if (node.getParentId() != null && node.getParentId() > 0) {
            Node parentNode = nodeStorage.get(node.getParentId());

            if (parentNode != null) {
                watcherManager.childrenListChanged(parentNode);
            }
        }
    }


    public void update(Node node) {
        nodeStorage.update(node);

        watcherManager.dataChanged(node);
    }

}
