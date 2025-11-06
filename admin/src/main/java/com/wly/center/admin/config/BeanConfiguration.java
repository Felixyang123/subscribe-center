package com.wly.center.admin.config;

import com.wly.center.admin.lock.OptimizeLocalHashMapLock;
import com.wly.center.admin.manager.NodeManager;
import com.wly.center.admin.manager.NodeStorageImpl;
import com.wly.center.admin.manager.WatcherManager;
import com.wly.center.admin.manager.WatcherStorageImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public WatcherManager watcherManager(WatcherStorageImpl watcherStorage) {
        return new WatcherManager(watcherStorage);
    }

    @Bean
    public NodeManager nodeManager(NodeStorageImpl nodeStorage, WatcherManager watcherManager, OptimizeLocalHashMapLock lock) {
        return new NodeManager(nodeStorage, watcherManager, lock);
    }
}
