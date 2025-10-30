package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Watcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WatcherStorageImpl implements WatcherStorage {
    private final WatcherCacheStorage cacheStorage;

    private final PersistWatcherStorage persistStorage;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(Watcher watcher) {
        persistStorage.add(watcher);

        cacheStorage.add(watcher);
    }

    @Override
    public List<Watcher> watchers(Long nodeId) {
        List<Watcher> watchers = cacheStorage.watchers(nodeId);
        if (!CollectionUtils.isEmpty(watchers)) {
            return watchers;
        }

        watchers = persistStorage.watchers(nodeId);
        watchers.forEach(cacheStorage::add);
        return watchers;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Watcher> remove(Long nodeId) {
        List<Watcher> watchers = persistStorage.remove(nodeId);

        cacheStorage.remove(nodeId);
        return watchers;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void remove(List<Watcher> watchers) {
        persistStorage.remove(watchers);

        cacheStorage.remove(watchers);
    }
}
