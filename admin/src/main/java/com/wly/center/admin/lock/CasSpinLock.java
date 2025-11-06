package com.wly.center.admin.lock;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class CasSpinLock implements Lock {
    private final AtomicReference<Thread> holder = new AtomicReference<>();

    @Override
    public boolean tryLock(String key) {
        return holder.compareAndSet(null, Thread.currentThread());
    }

    @Override
    public String lock(String key) {
        while (true) {
            if (holder.compareAndSet(null, Thread.currentThread())) {
                return key;
            }
            LockSupport.parkNanos(1);
        }
    }

    @Override
    public void unlock(String key) {
        holder.compareAndSet(Thread.currentThread(), null);
    }

    public static void main(String[] args) throws Exception {
        int[] num = new int[]{0};
        CasSpinLock lock = new CasSpinLock();
        java.util.List<Thread> ts = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
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
