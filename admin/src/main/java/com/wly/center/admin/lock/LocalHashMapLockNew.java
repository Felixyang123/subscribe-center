package com.wly.center.admin.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * Per-key Sync-based lock using AtomicReference (CAS) for owner and a
 * ConcurrentLinkedQueue for waiters. This implementation avoids synchronized
 * in Sync by relying on atomic operations and careful queue-head checks.
 */

@Slf4j
public class LocalHashMapLockNew implements Lock {

    private final ConcurrentMap<String, Sync> locks = new ConcurrentHashMap<>();

    private static class Sync {
        final AtomicReference<Thread> owner = new AtomicReference<>(null);
        final Queue<Thread> waiters = new ConcurrentLinkedQueue<>();
        private AtomicBoolean hold = new AtomicBoolean(false);

        boolean tryAcquire() {
            Thread t = Thread.currentThread();
            log.info("tryAcquire: {}", t.threadId());
            return owner.compareAndSet(null, t);
        }

        boolean enqueueAndTryAcquire() {
            if (!hold.compareAndSet(false, true)) {
                return false;
            }
            Thread t = Thread.currentThread();
            log.info("enqueueAndTryAcquire: {}", t.threadId());
            waiters.offer(t);
            // only the head may attempt to acquire owner to preserve FIFO
            Thread head = waiters.peek();
            if (head == t) {
                log.info("enqueueAndTryAcquire, head: {}", t.threadId());
                if (owner.compareAndSet(null, t)) {
                    // remove self from queue as we became owner
                    log.info("enqueueAndTryAcquire success: {}", t.threadId());
                    waiters.poll();
                    hold.set(false);
                    return true;
                }
            }
            log.info("enqueueAndTryAcquire fail: {}", t.threadId());
            hold.set(false);
            LockSupport.park();
            return false;
        }

        void releaseToNext() {
            while (true) {
                if (hold.compareAndSet(false, true)) {
                    log.info("releaseToNext: {}", Thread.currentThread().threadId());
                    owner.set(null);
                    Thread next = waiters.poll();
                    if (next != null) {
                        log.info("releaseToNext, next: {}", next.threadId());
                        // transfer ownership to next and unpark it
                        LockSupport.unpark(next);
                    }
                    hold.set(false);
                    return;
                }
            }
            // no waiter -> clear owner
        }

        boolean isFreeAndUnused() {
            return owner.get() == null && waiters.isEmpty();
        }

        boolean isOwner(Thread t) {
            return owner.get() == t;
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
            while (true) {
                if (v == null) {
                    v = new Sync();
                }

                if (v.tryAcquire()) {
                    return v;
                }

                log.info("tryAcquire fail: {}", Thread.currentThread().threadId());

                if (v.enqueueAndTryAcquire()) {
                    return v;
                }
            }
        });
        return key;
    }

    @Override
    public void unlock(String key) {
        log.info("unlock: {}", Thread.currentThread().threadId());
        Sync sync = locks.get(key);
        if (sync == null) {
            log.info("unlock, sync is null: {}", Thread.currentThread().threadId());
            return;
        }
        if (!sync.isOwner(Thread.currentThread())) {
            log.info("unlock, not owner: {}", Thread.currentThread().threadId());
            return;
        }

        sync.releaseToNext();
        locks.computeIfPresent(key, (k, v) -> {
            if (v.isFreeAndUnused()) {
                return null;
            }
            return v;
        });
    }

    // quick local main test
    public static void main(String[] args) throws Exception {
        int[] num = new int[]{0};
        LocalHashMapLockNew lock = new LocalHashMapLockNew();
        java.util.List<Thread> ts = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
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

        System.out.println("count=" + num[0] + ", ok=" + (num[0] == 10000));
    }
}

