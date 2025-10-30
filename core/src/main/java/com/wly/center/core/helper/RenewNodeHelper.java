package com.wly.center.core.helper;

import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.exception.BusinessExceptions;
import com.wly.center.core.pojo.req.OpenRenewNodeReq;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
@RequiredArgsConstructor
public class RenewNodeHelper {

    private final DelayQueue<RenewNodeTask> renewNodeTaskDelayQueue = new DelayQueue<>();

    private final ConcurrentMap<String, RenewNodeTask> renewNodeTaskMap = new ConcurrentHashMap<>();

    private final ExecutorService renewExecutor = Executors.newSingleThreadExecutor();

    private volatile boolean running = true;

    private final Long renewIntervalSeconds;

    public void start() {
        renewExecutor.execute(() -> {
            while (this.running) {
                RenewNodeTask task = null;
                try {
                    task = renewNodeTaskDelayQueue.take();

                    if (task.isDeleted()) {
                        continue;
                    }

                    addRenewNodeTask(task.getClient(), task.getNodeName());

                    task.run();
                } catch (Exception e) {
                    log.warn("renew node task error", e);

                    // 捕获NodeNotExistException后删除renewTask
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    } else if (task != null && e instanceof BusinessException businessException
                            && BusinessExceptions.NODE_NOT_EXIST.name().equals(businessException.getCode())) {
                        removeRenewNodeTask(task.getNodeName());
                    }
                }
            }
        });
    }

    public void stop() {
        this.running = false;
        renewExecutor.shutdownNow();
    }

    public void addRenewNodeTask(RestExchangeClient client, String nodeName) {
        RenewNodeTask task = new RenewNodeTask(client, nodeName, renewIntervalSeconds);
        renewNodeTaskDelayQueue.offer(task);
        renewNodeTaskMap.put(nodeName, task);
    }

    public void removeRenewNodeTask(String nodeName) {
        renewNodeTaskMap.computeIfPresent(nodeName, (k, v) -> {
            v.delete();
            return v;
        });
    }

    public static class RenewNodeTask implements Runnable, Delayed {
        @Getter
        private final RestExchangeClient client;
        @Getter
        private final String nodeName;
        private final Long nextRenewTime;
        private final Long expireAt;

        @Getter
        private boolean deleted = false;

        public RenewNodeTask(RestExchangeClient client,
                             String nodeName,
                             Long renewIntervalSeconds) {
            this.client = client;
            this.nodeName = nodeName;

            long renewIntervalMs = renewIntervalSeconds * 1000;
            this.nextRenewTime = System.currentTimeMillis() + renewIntervalMs;
            this.expireAt = this.nextRenewTime + renewIntervalMs * 3;
        }

        public void delete() {
            this.deleted = true;
        }

        @Override
        public void run() {
            client.renewNode(OpenRenewNodeReq.builder().nodeName(this.nodeName).expireAt(this.expireAt).build());
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return TimeUnit.NANOSECONDS.toNanos(nextRenewTime - System.currentTimeMillis());
        }

        @Override
        public int compareTo(Delayed o) {
            if (o == null) {
                return -1;
            }

            if (o instanceof RenewNodeTask task) {
                return Long.compare(this.nextRenewTime, task.nextRenewTime);
            }

            return Long.compare(this.getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
        }
    }
}
