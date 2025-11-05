package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Node;
import com.wly.center.core.exception.NodeExistException;

import java.util.concurrent.locks.ReentrantLock;

public record NodeManager(NodeStorage nodeStorage, WatcherManager watcherManager, ReentrantLock lock) {

    public NodeManager(NodeStorage nodeStorage, WatcherManager watcherManager) {
        this(nodeStorage, watcherManager, new ReentrantLock());
    }

    public void add(Node node) {
        lock.lock();
        try {
            if (!nodeStorage.add(node)) {
                throw new NodeExistException("节点已存在：" + node.getName());
            }
        } finally {
            lock.unlock();
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
