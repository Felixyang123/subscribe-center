package com.wly.center.common.lock;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.exception.BusinessExceptions;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import com.wly.center.core.utils.NetworkUtils;
import com.wly.center.core.watcher.NodeDeletedWatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public record UnfairLock(ExchangeServerFactory serverFactory) implements SubscribeLock {
    @Override
    public boolean tryLock(String lockName) {
        long tid = Thread.currentThread().threadId();
        try {
            innerLock(lockName, tid);
            return true;
        } catch (Exception e) {
            if (e instanceof BusinessException businessException && BusinessExceptions.NODE_EXIST.name().equals(businessException.getCode())) {
                logWarn("Lock failed: {}-{}, error: {}", tid, lockName, businessException.getMessage());
                return false;
            }
            logWarn("Lock failed: {}-{}", tid, lockName, e.getMessage());
            throw e;
        }
    }

    @Override
    public String lock(String lockName) {
        long tid = Thread.currentThread().threadId();
        try {
            innerLock(lockName, tid);
            return lockName;
        } catch (Exception lockException) {
            if (lockException instanceof BusinessException businessException && BusinessExceptions.NODE_EXIST.name().equals(businessException.getCode())) {
                logWarn("Lock failed: {}-{}, error: {}", tid, lockName, businessException.getMessage());

                WatchContext.addLockTrace("Lock failed, NODE_EXIST");
                if (!watch(lockName)) {
                    return lock(lockName);
                }
                WatchContext.addLockTrace("Thread park");
                LockSupport.park();
                WatchContext.addLockTrace("Thread unpark");
                return lock(lockName);
            } else {
                logWarn("Lock failed: {}-{}, error: {}", tid, lockName, lockException.getMessage());
                WatchContext.addLockTrace("Lock exception");
                throw lockException;
            }
        }
    }

    private boolean watch(String lockName) {
        Thread thread = Thread.currentThread();
        try {
            serverFactory.watchClient().watch(lockName, new UnfairLockWatcher(thread));
            log.debug("Watch success: {}, watch node created: {}", thread.threadId(), lockName);
            WatchContext.addLockTrace("Watch success");
            return true;
        } catch (Exception watchException) {
            if (watchException instanceof BusinessException watchBusinessException && BusinessExceptions.NODE_NOT_EXIST.name().equals(watchBusinessException.getCode())) {
                logWarn("Watch failed: {}, retry lock: {}, error: {}", thread.threadId(), lockName, watchBusinessException.getMessage());
                WatchContext.addLockTrace("Watch failed, NODE_NOT_EXIST");
                return false;
            } else {
                logWarn("Lock failed: {}, watch failed: {}, error: {}", thread.threadId(), lockName, watchException.getMessage());
                WatchContext.addLockTrace("Watch exception");
                throw watchException;
            }
        }
    }

    private static void logWarn(String logMsg, long tid, String lockName, String warnMsg) {
        log.warn(logMsg, tid, lockName, warnMsg);
    }

    private void innerLock(String lockName, long lockThreadId) {
        Map<String, Object> lockClientInfo = Map.of("clientIp", NetworkUtils.getServerIp(), "clientPort", serverFactory.port(), "threadId", lockThreadId);
        OpenNodeDetailResp lockNode = serverFactory.defaultNodeOptHelper().createTemporaryMasterNode(lockName, lockClientInfo, serverFactory().getExpireTimestamp());
        log.debug("Lock success: {}, lock node created: {}", lockThreadId, lockNode);
        WatchContext.addLockTrace("Lock success");
    }

    @Override
    public void unlock(String lockName) {
        WatchContext.addLockTrace("Unlock");
        long threadId = Thread.currentThread().threadId();
        try {
            OpenNodeDetailResp lockNode = serverFactory.defaultNodeOptHelper().getNodeDetail(lockName);

            log.debug("Unlock: {}, lock node removed: {}", threadId, lockNode);

            if (lockNode == null || !StringUtils.hasText(lockNode.getData())) {
                log.warn("No lock info found, node: {}-{}", threadId, lockName);
                WatchContext.addLockTrace("Unlock, No lock info found");
                return;
            }

            JSONObject lockClientInfo = JSON.parseObject(lockNode.getData());

            if (lockClientInfo.getLong("threadId").equals(threadId)) {
                serverFactory.defaultNodeOptHelper().removeNode(lockName);
                log.debug("Unlock success: {}, lock node removed: {}", threadId, lockNode);
                WatchContext.addLockTrace("Unlock success");
            }
        } catch (Exception e) {
            log.error("Unlock failed: {}-{}, error: {}", threadId, lockName, e.getMessage());
            WatchContext.addLockTrace("Unlock exception");
        }
    }

    public record UnfairLockWatcher(Thread tryLockThread, String key) implements NodeDeletedWatcher {
        public UnfairLockWatcher(Thread tryLockThread) {
            this(tryLockThread, UUID.randomUUID().toString().replace("-", ""));
        }

        @Override
        public void nodeDeleted(String nodeName) {
            log.debug("Lock released, retry lock: {} , lockName: {}", tryLockThread.threadId(), nodeName);
            LockSupport.unpark(tryLockThread);
        }

        @Override
        public String key() {
            return key;
        }
    }
}
