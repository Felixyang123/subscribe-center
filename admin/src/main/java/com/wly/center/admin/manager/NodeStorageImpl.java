package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Node;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NodeStorageImpl implements NodeStorage {
    private final CacheNodeStorage cacheStorage;
    private final PersistNodeStorage persistStorage;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(Node node) {
        return persistStorage.add(node) && cacheStorage.add(node);
    }

    @Override
    public Node get(Long id) {
        return Optional.ofNullable(cacheStorage.get(id)).orElseGet(() -> {
            Node node = persistStorage.get(id);
            if (node != null) {
                cacheStorage.add(node);
            }
            return node;
        });
    }

    @Override
    public Node get(String name) {
        return Optional.ofNullable(cacheStorage.get(name)).orElseGet(() -> {
            Node node = persistStorage.get(name);
            if (node != null) {
                cacheStorage.add(node);
            }
            return node;
        });
    }

    @Override
    public List<Node> children(Long parentId) {
        List<Node> nodes = cacheStorage.children(parentId);
        if (nodes.isEmpty()) {
            nodes = persistStorage.children(parentId);
            nodes.forEach(cacheStorage::add);
        }
        return nodes;
    }

    @Override
    public List<Node> children(String parentName) {
        List<Node> nodes = cacheStorage.children(parentName);
        if (nodes.isEmpty()) {
            nodes = persistStorage.children(parentName);
            nodes.forEach(cacheStorage::add);
        }
        return nodes;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Node remove(Long id) {
        Node node = persistStorage.remove(id);

        cacheStorage.remove(id);

        return node;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Node remove(String name) {
        Node node = persistStorage.remove(name);

        cacheStorage.remove(name);

        return node;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Node node) {
        persistStorage.update(node);

        cacheStorage.update(node);
    }
}
