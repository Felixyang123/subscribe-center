package com.wly.center.admin.lock;

public interface Lock {

    boolean tryLock(String key);

    String lock(String key);

    void unlock(String key);
}
