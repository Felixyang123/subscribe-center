package com.wly.center.admin.lock;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public record LockTemplate(Lock lock) {

    public void lockThenExecute(String key, Runnable runnable) {
        lock.lock(key);
        try {
            runnable.run();
        } finally {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        lock.unlock(key);
                    }
                });
            } else {
                lock.unlock(key);
            }
        }
    }
}
