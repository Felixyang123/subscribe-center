package com.wly.center.admin.manager;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wly.center.admin.dao.entity.Watcher;
import com.wly.center.admin.dao.rep.WatcherRep;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public record PersistWatcherStorage(WatcherRep watcherRep) implements WatcherStorage {
    @Override
    public void add(Watcher watcher) {
        watcherRep.save(watcher);
    }

    @Override
    public List<Watcher> watchers(Long nodeId) {
        return watcherRep.list(Wrappers.<Watcher>lambdaQuery().eq(Watcher::getNodeId, nodeId));
    }

    @Override
    public List<Watcher> remove(Long nodeId) {
        List<Watcher> watchers = watcherRep.list(Wrappers.<Watcher>lambdaQuery().eq(Watcher::getNodeId, nodeId));
        if (!watchers.isEmpty()) {
            watcherRep.removeBatchByIds(watchers);
        }
        return watchers;
    }

    @Override
    public void remove(List<Watcher> watchers) {
        if (!CollectionUtils.isEmpty(watchers)) {
            watcherRep.removeBatchByIds(watchers);
        }
    }
}
