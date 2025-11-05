package com.wly.center.common.lock;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wly.center.core.enumeration.WatcherCycleEnum;
import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.exception.BusinessExceptions;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import com.wly.center.core.utils.NetworkUtils;
import com.wly.center.core.watcher.Watcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public record FairLock(ExchangeServerFactory serverFactory) implements SubscribeLock {

    @Override
    public boolean tryLock(String lockName) {
        try {
            Map<String, Object> lockClientInfo = Map.of("clientIp", NetworkUtils.getServerIp(), "clientPort",
                    serverFactory.port(), "threadId", Thread.currentThread().threadId());
            OpenNodeDetailResp node = serverFactory.defaultNodeOptHelper().createTemporarySlaveNode(lockName,
                    lockClientInfo, serverFactory.getExpireTimestamp());

            List<OpenNodeDetailResp> children = serverFactory.watchClient().restExchangeClient().children(lockName);

            children.sort(Comparator.comparing(OpenNodeDetailResp::getId));

            return children.getFirst().getId().equals(node.getId());
        } catch (Exception e) {
            if (e instanceof BusinessException businessException && BusinessExceptions.NODE_NOT_EXIST.name()
                    .equals(businessException.getCode())) {
                return tryLock(lockName);
            }

            logWarn("Lock failed: {}, error: {}", lockName, e.getMessage());
            throw e;
        }
    }

    @Override
    public String lock(String lockName) {
        OpenNodeDetailResp node = createNode(lockName);

        if (doLock(lockName, node)) {
            return node.getName();
        }

        throw new BusinessException(BusinessExceptions.NODE_NOT_EXIST.name(), "No lock node found");
    }

    private boolean doLock(String lockName, OpenNodeDetailResp node) {
        List<OpenNodeDetailResp> children = children(lockName, node);

        children.sort(Comparator.comparing(OpenNodeDetailResp::getId));

        for (int i = 0; i < children.size(); i++) {
            OpenNodeDetailResp child = children.get(i);

            if (!child.getId().equals(node.getId())) {
                continue;
            }

            if (i == 0) {
                log.debug("Lock success, lock node created: {}", node);
                return true;
            }

            OpenNodeDetailResp prev = children.get(i - 1);

            if (!watch(lockName, prev, node)) {
                return doLock(lockName, node);
            }

            LockSupport.park();
            return doLock(lockName, node);
        }
        return false;
    }

    private boolean watch(String lockName, OpenNodeDetailResp prev, OpenNodeDetailResp node) {
        try {
            serverFactory.watchClient().watch(prev.getName(), new FairLockWatcher(serverFactory, Thread.currentThread(), lockName));
            WatchContext.addWatchCount(prev.getId());
            WatchContext.addWatchNodeId(prev.getId(), node.getId());
            WatchContext.addWatchNode(prev.getId(), node);
            return true;
        } catch (Exception e) {
            if (e instanceof BusinessException businessException && BusinessExceptions.NODE_NOT_EXIST.name().equals(businessException.getCode())) {
                return false;
            }

            serverFactory.defaultNodeOptHelper().removeNode(node.getName());
            logWarn("Lock failed: {}, error: {}", lockName, e.getMessage());
            throw e;
        }
    }

    public List<OpenNodeDetailResp> children(String lockName, OpenNodeDetailResp node) {

        try {
            return serverFactory.watchClient().restExchangeClient().children(lockName);
        } catch (Exception e) {
            serverFactory.defaultNodeOptHelper().removeNode(node.getName());
            throw e;
        }
    }

    private OpenNodeDetailResp createNode(String lockName) {
        Map<String, Object> lockClientInfo = Map.of("clientIp", NetworkUtils.getServerIp(), "clientPort",
                serverFactory.port(), "threadId", Thread.currentThread().threadId());
        return serverFactory.defaultNodeOptHelper().createTemporarySlaveNode(lockName,
                lockClientInfo, serverFactory.getExpireTimestamp());
    }

    private static void logWarn(String logMsg, String lockName, String... warnMsg) {
        log.warn(logMsg, lockName, warnMsg);
    }


    @Override
    public void unlock(String lockSlaveNodeName) {
        OpenNodeDetailResp lockNode = serverFactory.defaultNodeOptHelper().getNodeDetail(lockSlaveNodeName);
        if (lockNode == null || !StringUtils.hasText(lockNode.getData())) {
            logWarn("No lock info found, node: {}", lockSlaveNodeName);
            return;
        }

        JSONObject lockClientInfo = JSON.parseObject(lockNode.getData());

        if (lockClientInfo.getLong("threadId").equals(Thread.currentThread().threadId())) {
            serverFactory.defaultNodeOptHelper().removeNode(lockSlaveNodeName);
            log.debug("Unlock success, lock node removed: {}", lockNode);
        }
    }

    public record FairLockWatcher(ExchangeServerFactory serverFactory,
                                  Thread tryLockThread,
                                  String lockName,
                                  String key) implements Watcher {

        public FairLockWatcher(ExchangeServerFactory serverFactory, Thread tryLockThread, String lockName) {
            this(serverFactory, tryLockThread, lockName, UUID.randomUUID().toString().replace("-", ""));
        }

        @Override
        public void nodeDeleted(String nodeName) {
            log.debug("Lock released, lockName: {}, temporary slave node removed: {}", lockName, nodeName);
            LockSupport.unpark(tryLockThread);
        }

        @Override
        public void nodeDataChanged(String nodeName) {

        }

        @Override
        public WatcherCycleEnum cycleType() {
            return WatcherCycleEnum.SINGLE;
        }

        @Override
        public String key() {
            return key;
        }
    }
}
