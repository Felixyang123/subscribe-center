package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.lock.OptimizeLocalHashMapLock;
import com.wly.center.core.exception.NodeExistException;

public record NodeManager(NodeStorage nodeStorage, WatcherManager watcherManager, OptimizeLocalHashMapLock lock) {

    public void add(Node node) {
        lock.lock(node.getName());
        try {
            if (!nodeStorage.add(node)) {
                throw new NodeExistException("节点已存在：" + node.getName());
            }
        } finally {
            lock.unlock(node.getName());
        }
    }

    public void remove(Node node) {
        if (node.getId() != null) {
            nodeStorage.remove(node.getId());
        } else {
            node = nodeStorage.get(node.getName());
            nodeStorage.remove(node.getName());
        }

        watcherManager.nodeDeleted(node);
    }

    public void remove(String name) {
        Node node = nodeStorage.remove(name);

        watcherManager.nodeDeleted(node);
    }


    public void update(Node node) {
        nodeStorage.update(node);

        watcherManager.dataChanged(node);
    }

}
