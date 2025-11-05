package com.wly.center.common.lock;

import com.alibaba.fastjson2.JSON;
import com.wly.center.common.date.DateTimeUtils;
import com.wly.center.common.files.FileAppendUtils;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class WatchContext {

    private static final ConcurrentMap<Long, AtomicInteger> watchCount = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, CopyOnWriteArrayList<Long>> watchNodeIds = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, CopyOnWriteArrayList<OpenNodeDetailResp>> watchNodes = new ConcurrentHashMap<>();

    private static final AtomicInteger traceSort = new AtomicInteger(0);


    @Data
    @AllArgsConstructor
    public static class TraceEntity {
        private Long tid;

        private String trace;

        private Integer sort;
    }

    public static synchronized void addLockTrace(String trace) {
        int sort = traceSort.getAndIncrement();
        TraceEntity traceEntity = new TraceEntity(Thread.currentThread().threadId(), trace, sort);

        Path path = Paths.get("D:\\code\\subscribe-center\\subscribe-utils\\src\\main\\resources\\files\\lock_trace.json");
        try {
            Files.createFile(path);
            FileAppendUtils.appendLine(path, JSON.toJSONString(traceEntity));
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                try {
                    FileAppendUtils.appendLine(path, JSON.toJSONString(traceEntity));
                } catch (Exception exception) {
                    log.error("write lock trace error", exception);
                }
            } else {
                log.error("write lock trace error", e);
            }
        }
    }

    public static void formatTrace() {
        Path path = Paths.get("D:\\code\\subscribe-center\\subscribe-utils\\src\\main\\resources\\files\\lock_trace.json");
        Path path1 = Paths.get("D:\\code\\subscribe-center\\subscribe-utils\\src\\main\\resources\\files\\lock_trace_sort.json");
        try {
            List<String> traceJsons = Files.readAllLines(path);

            List<TraceEntity> traceEntities = JSON.parseArray(String.valueOf(traceJsons), TraceEntity.class);

            traceEntities.sort(Comparator.comparing(TraceEntity::getTid));

            Map<Long, List<TraceEntity>> tracesMap = traceEntities.stream().collect(Collectors.groupingBy(TraceEntity::getTid));

            tracesMap.forEach((k, v) -> {
                v.sort(Comparator.comparing(TraceEntity::getSort));
            });

            FileAppendUtils.appendLine(path1, JSON.toJSONString(tracesMap));
        } catch (Exception e) {
            log.error("read lock trace error", e);
        }
    }

    public static void main(String[] args) throws Exception {
//        List<Thread> ts = new ArrayList<>();
//        for (int i = 0; i < 3; i++) {
//            Thread t = new Thread(() -> {
//                addLockTrace("start");
//
//                addLockTrace("middle");
//
//                addLockTrace("end");
//            });
//            ts.add(t);
//            t.start();
//        }
//
//        for (Thread t : ts) {
//            t.join();
//        }

        formatTrace();
    }


    public static void addWatchCount(Long nodeId) {
        watchCount.computeIfAbsent(nodeId, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public static void addWatchNodeId(Long nodeId, Long watchNodeId) {
        watchNodeIds.computeIfAbsent(nodeId, k -> new CopyOnWriteArrayList<>()).add(watchNodeId);
    }

    public static void addWatchNode(Long nodeId, OpenNodeDetailResp watchNode) {
        watchNodes.computeIfAbsent(nodeId, k -> new CopyOnWriteArrayList<>()).add(watchNode);
    }


    public static void printWatchCount() throws IOException {
        String dateStr = DateTimeUtils.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String filename = "watch_count_" + dateStr + ".txt";
        String nodeIdFile = "watch_nodeid_" + dateStr + ".txt";
        String nodeFile = "watch_node_" + dateStr + ".txt";
        Path path = Files.createFile(Paths.get("D:\\code\\subscribe-center\\subscribe-utils\\src\\main\\resources\\files\\" + filename));
        Path path1 = Files.createFile(Paths.get("D:\\code\\subscribe-center\\subscribe-utils\\src\\main\\resources\\files\\" + nodeIdFile));
        Path path2 = Files.createFile(Paths.get("D:\\code\\subscribe-center\\subscribe-utils\\src\\main\\resources\\files\\" + nodeFile));

        watchCount.forEach((k, v) -> {
            if (v.get() == 1) {
                watchCount.remove(k);

                watchNodeIds.remove(k);

                watchNodes.remove(k);
            }
        });
        Files.writeString(path, JSON.toJSONString(watchCount));
        Files.writeString(path1, JSON.toJSONString(watchNodeIds));
        Files.writeString(path2, JSON.toJSONString(watchNodes));
    }

}
