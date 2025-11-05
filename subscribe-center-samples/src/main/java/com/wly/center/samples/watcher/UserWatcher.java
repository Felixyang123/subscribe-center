package com.wly.center.samples.watcher;

import com.wly.center.core.enumeration.WatcherCycleEnum;
import com.wly.center.core.helper.RestExchangeClient;
import com.wly.center.core.watcher.DataChangedWatcher;
import com.wly.center.samples.bean.User;
import com.wly.center.samples.context.UserContext;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class UserWatcher extends DataChangedWatcher<User> {
    private final String key;

    public UserWatcher(RestExchangeClient client) {
        super(client, User.class);
        this.key = UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public void handle(String nodeName, User data) {
        User old = UserContext.put(data);
        log.info("user data changed: nodeName={}, old={}, new={}", nodeName, old, data);
    }

    @Override
    public void nodeDeleted(String nodeName) {
        User user = UserContext.remove(nodeName);
        log.info("user removed: {}", user);
    }

    @Override
    public WatcherCycleEnum cycleType() {
        return WatcherCycleEnum.SINGLE;
    }

    @Override
    public String key() {
        return this.key;
    }
}
