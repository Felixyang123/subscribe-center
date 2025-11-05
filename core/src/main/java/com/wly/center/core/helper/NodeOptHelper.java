package com.wly.center.core.helper;

import com.alibaba.fastjson2.JSON;
import com.wly.center.core.pojo.req.OpenAddNodeReq;
import com.wly.center.core.pojo.req.OpenUpdateNodeReq;
import com.wly.center.core.pojo.resp.OpenAddNodeResp;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import com.wly.center.core.watcher.RenewNodeTaskWatcher;
import com.wly.center.core.watcher.WatchClient;

import java.util.Optional;

public record NodeOptHelper(WatchClient watchClient, RenewNodeHelper renewNodeHelper) {

    public OpenAddNodeResp createNode(String nodeName, Object value, Long expireAt, Boolean slave, Boolean temporary) {
        return watchClient.restExchangeClient().addNode(
                OpenAddNodeReq.builder()
                        .name(nodeName)
                        .data(Optional.ofNullable(value).map(JSON::toJSONString).orElse(null))
                        .expireAt(expireAt)
                        .slave(slave)
                        .temporary(temporary)
                        .build());
    }

    public OpenNodeDetailResp createMasterNode(String nodeName, Object value, Long expireAt, Boolean temporary) {
        OpenAddNodeResp resp = createNode(nodeName, value, expireAt, false, temporary);
        return Optional.ofNullable(resp).map(OpenAddNodeResp::getMasterNode).orElse(null);
    }


    public OpenNodeDetailResp createTemporaryMasterNode(String nodeName, Object value, Long expireAt) {
        OpenNodeDetailResp masterNode = createMasterNode(nodeName, value, expireAt, true);

        RenewNodeHelper.RenewNodeTask renewNodeTask = renewNodeHelper.addRenewNodeTask(watchClient.restExchangeClient(), masterNode.getName());

        watchClient.watch(nodeName, new RenewNodeTaskWatcher(renewNodeHelper, watchClient.restExchangeClient(), renewNodeTask.getKey()));
        return masterNode;
    }

    public OpenNodeDetailResp createPersistMasterNode(String nodeName, Object value) {
        return createMasterNode(nodeName, value, -1L, false);
    }

    public OpenNodeDetailResp createSlaveNode(String nodeName, Object value, Long expireAt, Boolean temporary) {
        OpenAddNodeResp resp = createNode(nodeName, value, expireAt, true, temporary);
        return Optional.ofNullable(resp).map(OpenAddNodeResp::getSlaveNode).orElse(null);
    }

    public OpenNodeDetailResp createTemporarySlaveNode(String nodeName, Object value, Long expireAt) {
        OpenNodeDetailResp slaveNode = createSlaveNode(nodeName, value, expireAt, true);

        RenewNodeHelper.RenewNodeTask renewNodeTask = renewNodeHelper.addRenewNodeTask(watchClient.restExchangeClient(), slaveNode.getName());

        watchClient.watch(slaveNode.getName(), new RenewNodeTaskWatcher(renewNodeHelper, watchClient.restExchangeClient(), renewNodeTask.getKey()));
        return slaveNode;
    }

    public OpenNodeDetailResp createPersistSlaveNode(String nodeName, Object value) {
        return createSlaveNode(nodeName, value, -1L, false);
    }

    public void removeNode(String nodeName) {
        watchClient.restExchangeClient().removeNode(nodeName);
    }

    public void updateNode(String nodeName, Object value) {
        watchClient.restExchangeClient().updateNode(OpenUpdateNodeReq.builder().name(nodeName).data(Optional.ofNullable(value).map(JSON::toJSONString).orElse(null)).build());
    }

    public OpenNodeDetailResp getNodeDetail(String nodeName) {
        return watchClient.restExchangeClient().getNodeDetail(nodeName);
    }
}
