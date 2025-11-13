package com.wly.center.samples.watcher;

import com.wly.center.core.watcher.NodeDeletedWatcher;
import com.wly.center.samples.bean.User;
import com.wly.center.samples.context.UserContext;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public record UserDeletedWatcher(String key) implements NodeDeletedWatcher {

    public UserDeletedWatcher() {
        this(UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public void nodeDeleted(String node) {
        User user = UserContext.remove(node);
        log.info("user removed: {}", user);
    }

    @Override
    public String key() {
        return key;
    }
}
