package com.wly.center.core.watcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocalWatcherManager {

    private final ConcurrentMap<String, ConcurrentMap<String, Watcher>> NODE_WATCHERS_MAP = new ConcurrentHashMap<>();

    private final CopyOnWriteArrayList<Watcher> GLOBAL_WATCHERS = new CopyOnWriteArrayList<>();

    public void addWatcher(String nodeName, Watcher watcher) {
        NODE_WATCHERS_MAP.computeIfAbsent(nodeName, k -> new ConcurrentHashMap<>()).put(watcher.key(), watcher);
    }

    public List<Watcher> getWatchers(String nodeName, boolean withGlobal) {
        List<Watcher> watchers = new ArrayList<>(Optional.ofNullable(NODE_WATCHERS_MAP.get(nodeName)).map(Map::values).orElseGet(ArrayList::new));

        if (withGlobal) {
            watchers.addAll(GLOBAL_WATCHERS);
        }
        return watchers;
    }

    public List<Watcher> getWatchers(String nodeName) {
        return getWatchers(nodeName, true);
    }

    public void removeWatchers(String nodeName, List<Watcher> watchers) {
        NODE_WATCHERS_MAP.computeIfPresent(nodeName, (k, v) -> {
            watchers.forEach(watcher -> v.remove(watcher.key()));
            return v;
        });
    }

    public void addGlobalWatcher(List<Watcher> globalWatchers) {
        Optional.ofNullable(globalWatchers).ifPresent(GLOBAL_WATCHERS::addAll);
    }
}
