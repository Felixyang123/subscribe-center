package com.wly.center.admin.config;

import com.wly.center.admin.lock.Lock;
import com.wly.center.admin.lock.LockTemplate;
import com.wly.center.admin.lock.OptimizeLocalHashMapLock;
import com.wly.center.admin.manager.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public WatcherManager watcherManager(WatcherStorageImpl watcherStorage, LockTemplate lockTemplate) {
        return new WatcherManager(watcherStorage, lockTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public Lock lock() {
        return new OptimizeLocalHashMapLock();
    }

    @Bean
    public LockTemplate lockTemplate(Lock lock) {
        return new LockTemplate(lock);
    }

    @Bean
    public NodeManager nodeManager(NodeStorageImpl nodeStorage, WatcherManager watcherManager, LockTemplate lockTemplate) {
        return new NodeManager(nodeStorage, watcherManager, lockTemplate);
    }
}
