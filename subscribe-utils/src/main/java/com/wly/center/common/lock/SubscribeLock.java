package com.wly.center.common.lock;

public interface SubscribeLock {

    boolean tryLock(String lockName);

    String lock(String lockName);

    void unlock(String lockName);
}
