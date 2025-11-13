package com.wly.center.samples.controller;

import com.alibaba.fastjson2.JSON;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.core.pojo.Result;
import com.wly.center.core.pojo.req.OpenUpdateNodeReq;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import com.wly.center.samples.bean.User;
import com.wly.center.samples.context.UserContext;
import com.wly.center.samples.watcher.UserChangedWatcher;
import com.wly.center.samples.watcher.UserDeletedWatcher;
import com.wly.center.starter.config.SubscribeCenterProps;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/sample/user")
@RequiredArgsConstructor
public class UserController {
    private final ExchangeServerFactory serverFactory;
    private final SubscribeCenterProps props;

    @RequestMapping("/add")
    public Result<OpenNodeDetailResp> addUser(@RequestBody User user) {
        OpenNodeDetailResp resp = serverFactory.defaultNodeOptHelper().createTemporaryMasterNode(user.getName(), user, System.currentTimeMillis() + 1000 * props.getRenewIntervalSeconds() * 3);

        serverFactory.watchClient().watch(user.getName(), new UserChangedWatcher(serverFactory.defaultExchangeClient()));
        serverFactory.watchClient().watch(user.getName(), new UserDeletedWatcher());
        UserContext.add(user);
        return Result.success(resp);
    }

    @PostMapping("/update")
    public Result<Void> updateUser(@RequestBody User user) {
        serverFactory.defaultExchangeClient().updateNode(
                OpenUpdateNodeReq.builder()
                        .name(user.getName())
                        .data(JSON.toJSONString(user))
                        .build());
        return Result.success();
    }

    @GetMapping("/detail/remote")
    public Result<User> getUserDetailRemote(@RequestParam(value = "name") String name) {
        var resp = serverFactory.defaultExchangeClient().getNodeDetail(name);
        return Result.success(Optional.ofNullable(resp).map(node -> JSON.parseObject(node.getData(), User.class)).orElse(null));
    }

    @GetMapping("/detail")
    public Result<User> getUserDetail(@RequestParam(value = "name") String name) {
        return Result.success(UserContext.get(name));
    }

    @PostMapping("/delete")
    public Result<Void> deleted(@RequestParam(value = "name") String name){
        serverFactory.defaultExchangeClient().removeNode(name);
        return Result.success();
    }
}
