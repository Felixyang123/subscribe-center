package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Watcher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class WatcherCacheStorage implements WatcherStorage {

    private final ConcurrentMap<Long, ConcurrentMap<Long, Watcher>> NODE_WATCHERS_MAP = new ConcurrentHashMap<>();

    @Override
    public void add(Watcher watcher) {
        NODE_WATCHERS_MAP.computeIfAbsent(watcher.getNodeId(), k -> new ConcurrentHashMap<>()).put(watcher.getId(), watcher);
    }

    @Override
    public List<Watcher> watchers(Long nodeId) {
        return Optional.ofNullable(NODE_WATCHERS_MAP.get(nodeId)).map(ConcurrentMap::values).map(ArrayList::new).orElse(new ArrayList<>());
    }

    @Override
    public List<Watcher> remove(Long nodeId) {
        return Optional.ofNullable(NODE_WATCHERS_MAP.remove(nodeId)).map(ConcurrentMap::values).map(ArrayList::new).orElse(null);
    }

    @Override
    public void remove(List<Watcher> watchers) {
        watchers.forEach(watcher -> NODE_WATCHERS_MAP.get(watcher.getNodeId()).remove(watcher.getId()));
    }
}
