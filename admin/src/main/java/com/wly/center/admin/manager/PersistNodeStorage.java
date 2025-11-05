package com.wly.center.admin.manager;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.dao.rep.NodeRep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class PersistNodeStorage implements NodeStorage, SmartLifecycle {

    private final NodeRep nodeRep;

    private final WatcherManager watcherManager;

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public Boolean add(Node node) {
        try {
            nodeRep.save(node);
            return Boolean.TRUE;
        } catch (DuplicateKeyException e) {
            log.warn("Node exists: ", e);
            return Boolean.FALSE;
        }
    }

    @Override
    public Node get(Long id) {
        return nodeRep.getById(id);
    }

    @Override
    public Node get(String name) {
        return nodeRep.getOne(Wrappers.<Node>lambdaQuery().eq(Node::getName, name));
    }

    @Override
    public List<Node> children(Long parentId) {
        return nodeRep.list(Wrappers.<Node>lambdaQuery().eq(Node::getParentId, parentId));
    }

    @Override
    public List<Node> children(String parentName) {
        Node parentNode = nodeRep.getOne(Wrappers.<Node>lambdaQuery().eq(Node::getName, parentName));
        if (parentNode == null) {
            return List.of();
        }

        return nodeRep.list(Wrappers.<Node>lambdaQuery().eq(Node::getParentId, parentNode.getId()));
    }

    @Override
    public Node remove(Long id) {
        Node node = nodeRep.getById(id);
        if (nodeRep.removeById(id)) {
            return node;
        }
        return null;
    }

    @Override
    public Node remove(String name) {
        Node node = nodeRep.getOne(Wrappers.<Node>lambdaQuery().eq(Node::getName, name));
        if (nodeRep.remove(Wrappers.<Node>lambdaQuery().eq(Node::getName, name))) {
            return node;
        }
        return null;
    }

    @Override
    public void update(Node node) {
        nodeRep.updateById(node);
    }

    @Override
    public void start() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                List<Node> expiredNodes = nodeRep.list(Wrappers.<Node>lambdaQuery().ne(Node::getExpireAt, -1L).lt(Node::getExpireAt, System.currentTimeMillis()));

                if (!CollectionUtils.isEmpty(expiredNodes)) {
                    nodeRep.removeBatchByIds(expiredNodes);

                    expiredNodes.forEach(watcherManager::nodeDeleted);
                }
            } catch (Exception e) {
                log.error("Expired node check error: ", e);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        this.scheduledExecutor.shutdownNow();
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
