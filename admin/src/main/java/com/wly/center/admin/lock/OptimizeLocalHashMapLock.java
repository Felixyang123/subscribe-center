package com.wly.center.admin.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Per-key lock implementation that avoids non-atomic cleanup races by tracking
 * a holders counter inside LockEntry and updating it atomically via
 * ConcurrentHashMap.compute/computeIfPresent.
 */
public class OptimizeLocalHashMapLock implements Lock {

    private static class LockEntry {
        final ReentrantLock lock = new ReentrantLock();
        int holders = 0; // mutated only inside compute/computeIfPresent
    }

    private final ConcurrentMap<String, LockEntry> locks = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String key) {
        // Atomically create or increment holders
        LockEntry entry = locks.compute(key, (k, v) -> {
            if (v == null) v = new LockEntry();
            v.holders++;
            return v;
        });

        boolean acquired = false;
        try {
            acquired = entry.lock.tryLock();
            return acquired;
        } finally {
            if (!acquired) {
                // failed to acquire => decrement holders and maybe remove
                locks.computeIfPresent(key, (k, v) -> {
                    v.holders--;
                    if (v.holders == 0 && !v.lock.isLocked() && !v.lock.hasQueuedThreads()) {
                        return null;
                    }
                    return v;
                });
            }
        }
    }

    @Override
    public String lock(String key) {
        LockEntry entry = locks.compute(key, (k, v) -> {
            if (v == null) v = new LockEntry();
            v.holders++;
            return v;
        });

        entry.lock.lock();
        return key;
    }

    @Override
    public void unlock(String key) {
        LockEntry entry = locks.get(key);
        if (entry == null) {
            throw new IllegalMonitorStateException("No lock for key: " + key);
        }

        try {
            entry.lock.unlock();
        } catch (IllegalMonitorStateException e) {
            throw new IllegalMonitorStateException("Current thread does not hold lock for key: " + key);
        }

        // decrement holders and remove if possible (atomic w.r.t map ops)
        locks.computeIfPresent(key, (k, v) -> {
            v.holders--;
            if (v.holders == 0 && !v.lock.isLocked() && !v.lock.hasQueuedThreads()) {
                return null;
            }
            return v;
        });
    }

    // simple stress test main
    public static void main(String[] args) throws Exception {
        AtomicInteger num = new AtomicInteger(0);
        OptimizeLocalHashMapLock lock = new OptimizeLocalHashMapLock();
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    lock.lock("lock");
                    try {
                        num.incrementAndGet();
                    } finally {
                        lock.unlock("lock");
                    }
                }
            });
            ts.add(t);
            t.start();
        }

        for (Thread t : ts) t.join();

        System.out.println("count=" + num.get() + ", ok=" + (num.get() == 10000));
    }
}

