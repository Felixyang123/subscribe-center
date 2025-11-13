package com.wly.center.samples.watcher;

import com.wly.center.core.helper.RestExchangeClient;
import com.wly.center.core.watcher.AbstractDataChangedWatcher;
import com.wly.center.samples.bean.User;
import com.wly.center.samples.context.UserContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserChangedWatcher extends AbstractDataChangedWatcher<User> {

    public UserChangedWatcher(RestExchangeClient client) {
        super(client, User.class);
    }

    @Override
    public void dataChanged(String node, User data) {
        User old = UserContext.put(data);
        log.info("user data changed: node={}, old={}, new={}", node, old, data);
    }
}
