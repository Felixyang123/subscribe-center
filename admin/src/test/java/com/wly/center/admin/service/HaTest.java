package com.wly.center.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wly.center.admin.dao.entity.HaMasterInstance;
import com.wly.center.admin.dao.rep.HaMasterRep;
import com.wly.center.admin.dao.rep.HaMasterSelectSnapshotRep;
import com.wly.center.admin.enumeration.HaMasterSelectInstanceStatusEnum;
import com.wly.center.admin.pojo.req.HaMasterSelectReq;
import com.wly.center.admin.selector.MasterInitEvent;
import com.wly.center.admin.selector.MasterInitListener;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Future;

@SpringBootTest
public class HaTest {

    @Resource
    private HaMasterRep haMasterRep;

    @Resource
    private HaMasterSelectSnapshotRep snapshotRep;

    @Resource
    private TransactionTemplate txTemplate;

    @Resource
    private MasterInitListener masterInitListener;

    @Resource
    private ApplicationEventMulticaster eventMulticaster;

    @Test
    @DisplayName("测试HA主从选举")
    @SneakyThrows
    void HaElectionTest() {
        HaMasterSelectService service = new HaMasterSelectService(haMasterRep, snapshotRep, txTemplate, masterInitListener);

        HaMasterSelectService service1 = new HaMasterSelectService(haMasterRep, snapshotRep, txTemplate, masterInitListener);

        Future<?> future = service.asyncServeAsMaster(HaMasterSelectReq.builder()
                .serviceName("test-ha-election")
                .host("127.0.0.1")
                .port(9001)
                .renewIntervalSeconds(1L)
                .build());

        future.get();

        HaMasterInstance masterInstance = haMasterRep.getOne(Wrappers.<HaMasterInstance>lambdaQuery()
                .eq(HaMasterInstance::getServiceName, "test-ha-election"));

        Assertions.assertNotNull(masterInstance);
        Assertions.assertEquals(HaMasterSelectInstanceStatusEnum.SERVING.getCode(), masterInstance.getInstanceStatus());
        Assertions.assertEquals("127.0.0.1", masterInstance.getHost());
        Assertions.assertEquals(9001, masterInstance.getPort());


        eventMulticaster.multicastEvent(new MasterInitEvent(new Object()));

        service1.asyncServeAsMaster(HaMasterSelectReq.builder()
                .serviceName("test-ha-election")
                .host("127.0.0.1")
                .port(9002)
                .renewIntervalSeconds(1L)
                .build());

        Thread.sleep(1000);

        HaMasterInstance masterInstance1 = haMasterRep.getOne(Wrappers.<HaMasterInstance>lambdaQuery()
                .eq(HaMasterInstance::getServiceName, "test-ha-election"));

        Assertions.assertNotNull(masterInstance1);
        Assertions.assertEquals(HaMasterSelectInstanceStatusEnum.SERVING.getCode(), masterInstance1.getInstanceStatus());
        Assertions.assertEquals("127.0.0.1", masterInstance1.getHost());
        Assertions.assertEquals(9001, masterInstance1.getPort());

        service.close();
        Thread.sleep(4000);

        HaMasterInstance masterInstance2 = haMasterRep.getOne(Wrappers.<HaMasterInstance>lambdaQuery()
                .eq(HaMasterInstance::getServiceName, "test-ha-election"));

        Assertions.assertNotNull(masterInstance2);
        Assertions.assertEquals(HaMasterSelectInstanceStatusEnum.SERVING.getCode(), masterInstance2.getInstanceStatus());
        Assertions.assertEquals("127.0.0.1", masterInstance2.getHost());
        Assertions.assertEquals(9002, masterInstance2.getPort());
        service1.close();
    }

    @Test
    @DisplayName("测试关闭主从选举")
    @SneakyThrows
    void HaDownTest() {
        HaMasterSelectService service = new HaMasterSelectService(haMasterRep, snapshotRep, txTemplate, masterInitListener);
        Future<?> future = service.asyncServeAsMaster(HaMasterSelectReq.builder()
                .serviceName("test-ha-close")
                .host("127.0.0.1")
                .port(9000)
                .renewIntervalSeconds(1L)
                .build());

        future.get();

        HaMasterInstance masterInstance = haMasterRep.getOne(Wrappers.<HaMasterInstance>lambdaQuery()
                .eq(HaMasterInstance::getServiceName, "test-ha-close"));

        Assertions.assertNotNull(masterInstance);
        Assertions.assertEquals(HaMasterSelectInstanceStatusEnum.SERVING.getCode(), masterInstance.getInstanceStatus());
        Assertions.assertEquals("127.0.0.1", masterInstance.getHost());
        Assertions.assertEquals(9000, masterInstance.getPort());

        service.close();

        Thread.sleep(4000);

        masterInstance = haMasterRep.getOne(Wrappers.<HaMasterInstance>lambdaQuery()
                .eq(HaMasterInstance::getServiceName, "test-ha-close"));
        Assertions.assertNotNull(masterInstance);
        Assertions.assertTrue(System.currentTimeMillis() > masterInstance.getExpireAt());
    }

    @Test
    @DisplayName("测试续约任务")
    @SneakyThrows
    void HaRenewTest() {
        try (HaMasterSelectService service = new HaMasterSelectService(haMasterRep, snapshotRep, txTemplate, masterInitListener)) {
            Future<?> future = service.asyncServeAsMaster(HaMasterSelectReq.builder()
                    .serviceName("test-ha-close")
                    .host("127.0.0.1")
                    .port(9000)
                    .renewIntervalSeconds(1L)
                    .build());

            future.get();

            HaMasterInstance masterInstance = haMasterRep.getOne(Wrappers.<HaMasterInstance>lambdaQuery()
                    .eq(HaMasterInstance::getServiceName, "test-ha-close"));

            Assertions.assertNotNull(masterInstance);
            Assertions.assertEquals(HaMasterSelectInstanceStatusEnum.SERVING.getCode(), masterInstance.getInstanceStatus());
            Assertions.assertEquals("127.0.0.1", masterInstance.getHost());
            Assertions.assertEquals(9000, masterInstance.getPort());

            Thread.sleep(4000);

            masterInstance = haMasterRep.getOne(Wrappers.<HaMasterInstance>lambdaQuery()
                    .eq(HaMasterInstance::getServiceName, "test-ha-close"));
            Assertions.assertNotNull(masterInstance);
            Assertions.assertTrue(System.currentTimeMillis() < masterInstance.getExpireAt());
        }

    }
}
