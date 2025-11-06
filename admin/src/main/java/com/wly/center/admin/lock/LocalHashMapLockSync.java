package com.wly.center.admin.lock;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.LockSupport;

/**
 * Per-key Sync-based lock that preserves LockSupport park/unpark semantics.
 * This is a sibling implementation to the original LocalHashMapLock to verify
 * the per-key approach without modifying the original file (insert edit mapping issues).
 */
public class LocalHashMapLockSync implements Lock {

    private final ConcurrentMap<String, Sync> locks = new ConcurrentHashMap<>();

    private static class Sync {
        volatile Thread owner;
        final Queue<Thread> waiters = new LinkedBlockingQueue<>();

        synchronized boolean tryAcquire() {
            if (owner == null) {
                owner = Thread.currentThread();
                return true;
            }
            return false;
        }

        synchronized boolean enqueueAndTryAcquire() {
            Thread t = Thread.currentThread();
            waiters.offer(t);
            Thread head = waiters.peek();
            if (owner == null && head == t) {
                if (tryAcquire()) {
                    waiters.poll();
                    return true;
                }
            }
            return false;
        }

        synchronized void releaseToNext() {
            Thread next = waiters.poll();
            if (next != null) {
                LockSupport.unpark(next);
            }
            owner = null;
        }

        synchronized boolean isFreeAndUnused() {
            return owner == null && waiters.isEmpty();
        }

        synchronized boolean isOwner(Thread t) {
            return owner == t;
        }
    }

    @Override
    public boolean tryLock(String key) {
        Sync sync = locks.computeIfAbsent(key, k -> new Sync());
        return sync.tryAcquire();
    }

    @Override
    public String lock(String key) {
        locks.compute(key, (k, v) -> {
            if (v == null) {
                v = new Sync();
            }

            if (v.tryAcquire()) {
                return v;
            }
            while (true) {
                if (v.enqueueAndTryAcquire()) {
                    return v;
                }
                LockSupport.park();
                if (v.tryAcquire()) {
                    return v;
                }
            }
        });
        return key;
    }

    @Override
    public void unlock(String key) {
        Sync sync = locks.get(key);
        if (sync == null) return;
        if (!sync.isOwner(Thread.currentThread())) return;
        sync.releaseToNext();

        locks.computeIfPresent(key, (k, v) -> {
            if (v.isFreeAndUnused()) {
                return null;
            }
            return v;
        });
    }

    // quick local main test (same semantics as original)
    public static void main(String[] args) throws Exception {
        int[] num = new int[]{0};
        LocalHashMapLockSync lock = new LocalHashMapLockSync();
        java.util.List<Thread> ts = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    lock.lock("lock");
                    try {
                        num[0] = num[0] + 1;
                    } finally {
                        lock.unlock("lock");
                    }
                }
            });
            ts.add(t);
            t.start();
        }

        for (Thread t : ts) t.join();

        System.out.println("count=" + num[0]);
    }
}

