package com.wly.center.samples.watcher;

import com.wly.center.core.helper.RestExchangeClient;
import com.wly.center.core.watcher.DataChangedWatcher;
import com.wly.center.samples.bean.User;
import com.wly.center.samples.context.UserContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserWatcher extends DataChangedWatcher<User> {
    public UserWatcher(RestExchangeClient client) {
        super(client, User.class);
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
}
