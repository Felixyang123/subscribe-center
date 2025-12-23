package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.dao.rep.NodeRep;
import com.wly.center.admin.ha.MasterInitEvent;
import com.wly.center.core.enumeration.NodeTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheNodeStorage implements NodeStorage, SmartLifecycle {
    private final ConcurrentMap<Long, Node> NODE_MAP = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, CopyOnWriteArrayList<Node>> CHILD_NODE_MAP = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Long> NODE_NAME_ID_MAP = new ConcurrentHashMap<>();

    private final DelayQueue<NodeDelayed> DELAY_QUEUE = new DelayQueue<>();

    private volatile boolean running = true;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final NodeRep nodeRep;

    private final ApplicationEventMulticaster eventMulticaster;

    public Boolean add(Node node) {
        Long nodedId = NODE_NAME_ID_MAP.putIfAbsent(node.getName(), node.getId());
        if (nodedId != null) {
            // 节点存在
            return Boolean.FALSE;
        }

        NODE_MAP.put(node.getId(), node);

        if (NodeTypeEnum.SLAVE.getCode().equals(node.getType())) {
            CHILD_NODE_MAP.computeIfAbsent(node.getParentId(), k -> new CopyOnWriteArrayList<>()).add(node);
        }

        if (node.getExpireAt() != null && node.getExpireAt() > -1) {
            DELAY_QUEUE.offer(new NodeDelayed(node.getId(), node.getExpireAt()));
        }

        return Boolean.TRUE;
    }

    public Node get(Long id) {
        return NODE_MAP.get(id);
    }

    public Node get(String name) {
        return Optional.ofNullable(NODE_NAME_ID_MAP.get(name)).map(NODE_MAP::get).orElse(null);
    }

    public List<Node> children(Long parentId) {
        return CHILD_NODE_MAP.get(parentId);
    }

    public List<Node> children(String parentName) {
        return Optional.ofNullable(NODE_NAME_ID_MAP.get(parentName)).map(CHILD_NODE_MAP::get).orElse(null);
    }

    public Node remove(Long id) {
        Node node = NODE_MAP.remove(id);
        if (node != null) {
            NODE_NAME_ID_MAP.remove(node.getName());
            if (NodeTypeEnum.SLAVE.getCode().equals(node.getType())) {
                CHILD_NODE_MAP.computeIfPresent(node.getParentId(), (k, v) -> {
                    v.removeIf(n -> n.getId().equals(id));
                    return v;
                });
            } else {
                CHILD_NODE_MAP.remove(id);
            }
        }
        return node;
    }

    public Node remove(String name) {
        Long id = NODE_NAME_ID_MAP.remove(name);
        if (id != null) {
            return remove(id);
        }
        return null;
    }

    public void update(Node node) {
        NODE_MAP.computeIfPresent(node.getId(), (k, v) -> node);
        NODE_NAME_ID_MAP.computeIfPresent(node.getName(), (k, v) -> node.getId());
    }

    @Override
    public void start() {
        executorService.execute(this::run);
    }

    @Override
    public void stop() {
        this.running = false;
        executorService.shutdownNow();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private void run() {
        //TODO 优化全量查询
        List<Node> nodes = nodeRep.list();

        nodes.forEach(this::add);

        eventMulticaster.multicastEvent(new MasterInitEvent(new Object()));

        while (running) {
            try {
                NodeDelayed nodeDelayed = DELAY_QUEUE.take();

                Node node = get(nodeDelayed.nodeId);

                if (node == null) {
                    continue;
                }

                if (node.getExpireAt() == -1) {
                    continue;
                }

                if (System.currentTimeMillis() > node.getExpireAt()) {
                    remove(nodeDelayed.nodeId);
                } else {
                    DELAY_QUEUE.offer(new NodeDelayed(node.getId(), node.getExpireAt()));
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                } else {
                    log.warn("CacheNodeStorage run error", e);
                }
            }
        }
    }

    public record NodeDelayed(Long nodeId, Long expireAt) implements Delayed {

        @Override
        public long getDelay(TimeUnit unit) {
            return TimeUnit.NANOSECONDS.toNanos(this.expireAt - System.currentTimeMillis());
        }

        @Override
        public int compareTo(Delayed o) {
            if (o == null) {
                return -1;
            }

            if (o instanceof NodeDelayed delayed) {
                return Long.compare(this.expireAt, delayed.expireAt);
            }

            return Long.compare(this.getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
        }
    }
}
