package com.wly.center.core.helper;

import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.exception.BusinessExceptions;
import com.wly.center.core.pojo.req.OpenRenewNodeReq;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@RequiredArgsConstructor
public class RenewNodeHelper {

    private final DelayQueue<RenewNodeTask> renewNodeTaskDelayQueue = new DelayQueue<>();

    private final ConcurrentMap<String, RenewNodeTask> renewNodeTaskMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, CopyOnWriteArrayList<RenewNodeTask>> node2TasksMap = new ConcurrentHashMap<>();

    private final ExecutorService renewExecutor = Executors.newSingleThreadExecutor();

    private volatile boolean running = true;

    private final Long renewIntervalSeconds;

    public void start() {
        renewExecutor.execute(() -> {
            while (this.running) {
                RenewNodeTask task = null;
                try {
                    task = renewNodeTaskDelayQueue.take();

                    if (!renewNodeTaskMap.containsKey(task.getKey())) {
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
                        removeTasksByNode(task.getNodeName());
                    }
                }
            }
        });
    }

    public void stop() {
        this.running = false;
        renewExecutor.shutdownNow();
    }

    public RenewNodeTask addRenewNodeTask(RestExchangeClient client, String nodeName) {
        RenewNodeTask task = new RenewNodeTask(client, nodeName, renewIntervalSeconds);
        renewNodeTaskDelayQueue.offer(task);
        renewNodeTaskMap.put(task.getKey(), task);
        node2TasksMap.computeIfAbsent(nodeName, k -> new CopyOnWriteArrayList<>()).add(task);
        return task;
    }

    public void removeTasksByNode(String nodeName) {
        CopyOnWriteArrayList<RenewNodeTask> tasks = node2TasksMap.remove(nodeName);
        if (!CollectionUtils.isEmpty(tasks)) {
            tasks.forEach(task -> renewNodeTaskMap.remove(task.getKey()));
        }
    }

    public void removeTaskByKey(String key) {
        RenewNodeTask renewNodeTask = renewNodeTaskMap.remove(key);

        if (renewNodeTask != null) {
            node2TasksMap.computeIfPresent(renewNodeTask.getNodeName(), (k, v) -> {
                v.removeIf(task -> task.getKey().equals(key));
                return v;
            });
        }
    }

    public static class RenewNodeTask implements Runnable, Delayed {
        @Getter
        private final RestExchangeClient client;
        @Getter
        private final String nodeName;
        private final Long nextRenewTime;
        private final Long expireAt;

        @Getter
        private final String key;

        public RenewNodeTask(RestExchangeClient client,
                             String nodeName,
                             Long renewIntervalSeconds) {
            this.client = client;
            this.nodeName = nodeName;

            long renewIntervalMs = renewIntervalSeconds * 1000;
            this.nextRenewTime = System.currentTimeMillis() + renewIntervalMs;
            this.expireAt = this.nextRenewTime + renewIntervalMs * 3;
            this.key = UUID.randomUUID().toString().replace("-", "");
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
