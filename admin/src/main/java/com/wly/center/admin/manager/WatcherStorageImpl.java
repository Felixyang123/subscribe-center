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
    public List<Watcher> watchers(String nodeName) {
        List<Watcher> watchers = cacheStorage.watchers(nodeName);
        if (!CollectionUtils.isEmpty(watchers)) {
            return watchers;
        }

        watchers = persistStorage.watchers(nodeName);
        watchers.forEach(cacheStorage::add);
        return watchers;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Watcher> remove(String nodeName) {
        persistStorage.remove(nodeName);

        // 删除结果以内存为准，因为内存删除是线程安全的，保证CacheNodeStorage和PersistNodeStorage后台定时删除过期临时节点的任务在并发下只会出发一次watchers
        return cacheStorage.remove(nodeName);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void remove(List<Watcher> watchers) {
        persistStorage.remove(watchers);

        cacheStorage.remove(watchers);
    }
}
