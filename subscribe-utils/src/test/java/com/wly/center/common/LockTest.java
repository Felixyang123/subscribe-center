package com.wly.center.common;

import com.wly.center.common.lock.FairLock;
import com.wly.center.common.lock.UnfairLock;
import com.wly.center.core.factory.ExchangeServerFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

@Slf4j
public class LockTest {

    @Test
    void unfairLockTest() throws InterruptedException {
        ExchangeServerFactory factory = ExchangeServerFactory.builder()
                .defaultBaseUrl("http://localhost:8200")
                .port(8300)
                .renewIntervalSeconds(10L)
                .build();
        factory.start();
        int[] nums = new int[]{0};

        CyclicBarrier barrier = new CyclicBarrier(10);

        UnfairLock lock = new UnfairLock(factory);

        List<Thread> ts = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Thread t = getThread(barrier, lock, nums);
            ts.add(t);
        }

        for (Thread t : ts) {
            t.join();
        }

        System.out.println(nums[0]);
        Assertions.assertEquals(100, nums[0]);
        factory.stop();
    }


    private static Thread getThread(CyclicBarrier barrier, UnfairLock lock, int[] nums) {
        String key = "test-unfair-lock-11";
        Thread t = new Thread(() -> {
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }

            for (long j = 0; j < 10; j++) {
                lock.lock(key);
                log.info("lock success thread: 【{}】", Thread.currentThread().threadId());
                try {
                    int num = nums[0];
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    num++;
                    nums[0] = num;
                    log.info("num: {}-{}", num, Thread.currentThread().threadId());
                } finally {
                    log.info("unlock thread: 【{}】", Thread.currentThread().threadId());
                    lock.unlock(key);
                }
            }
        });
        t.start();
        return t;
    }


    @Test
    void unfairLockTest2() throws InterruptedException {
        ExchangeServerFactory factory = ExchangeServerFactory.builder()
                .defaultBaseUrl("http://localhost:8200")
                .port(8300)
                .renewIntervalSeconds(10L)
                .build();
        factory.start();

        UnfairLock lock = new UnfairLock(factory);

        CountDownLatch latch = new CountDownLatch(2);
        new Thread(() -> {
            lock.lock("test-unfair-lock-2");
            try {
                log.info("lock thread: {}", Thread.currentThread().getName());
                Thread.sleep(1000L);
                latch.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock("test-unfair-lock-2");
            }
        }).start();
        new Thread(() -> {
            lock.lock("test-unfair-lock-2");
            try {
                log.info("lock thread: {}", Thread.currentThread().getName());
                Thread.sleep(1000L);
                latch.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock("test-unfair-lock-2");
            }
        }).start();

        latch.await();

        log.info("test end....");
    }

    @Test
    void fairLockTest() throws InterruptedException, IOException {
        ExchangeServerFactory factory = ExchangeServerFactory.builder()
                .defaultBaseUrl("http://localhost:8200")
                .port(8300)
                .renewIntervalSeconds(30L)
                .build();
        factory.start();
        int[] nums = new int[]{0};

        CyclicBarrier barrier = new CyclicBarrier(30);

        FairLock lock = new FairLock(factory);

        List<Thread> ts = new ArrayList<>();

        String key = "test-unfair-lock-4";
        for (int i = 0; i < 30; i++) {
            Thread t = new Thread(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }

                for (long j = 0; j < 50; j++) {
                    String lockKey = null;
                    try {
                        lockKey = lock.lock(key);
                        int num = nums[0];
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        num++;
                        nums[0] = num;
                    } catch (Exception e) {
                        log.error("lock error: ", e);
                    } finally {
                        if (lockKey != null) {
                            lock.unlock(lockKey);
                        }
                    }
                }
            });
            t.start();
            ts.add(t);
        }

        for (Thread t : ts) {
            t.join();
        }

//        WatchContext.printWatchCount();

        System.out.println(nums[0]);
        Assertions.assertEquals(1500, nums[0]);
        factory.stop();
    }
}
