package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Watcher;

import java.util.List;

public interface WatcherStorage {

    void add(Watcher watcher);

    List<Watcher> watchers(Long nodeId);

    List<Watcher> remove(Long nodeId);

    void remove(List<Watcher> watchers);
}
