package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Watcher;

import java.util.List;

public interface WatcherStorage {

    void add(Watcher watcher);

    List<Watcher> watchers(String nodeName);

    List<Watcher> remove(String nodeName);

    void remove(List<Watcher> watchers);
}
