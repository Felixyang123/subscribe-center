package com.wly.center.admin.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wly.center.admin.common.BeanConvertor;
import com.wly.center.admin.dao.entity.HaMasterInstance;
import com.wly.center.admin.dao.entity.HaMasterSelectSnapshot;
import com.wly.center.admin.dao.rep.HaMasterRep;
import com.wly.center.admin.dao.rep.HaMasterSelectSnapshotRep;
import com.wly.center.admin.enumeration.HaMasterSelectInstanceStatusEnum;
import com.wly.center.admin.pojo.req.HaMasterSelectReq;
import com.wly.center.admin.ha.MasterInitListener;
import com.wly.center.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

@Service
@Slf4j
public class HaMasterSelectService implements Destroyable, AutoCloseable {

    private final HaMasterRep haMasterRep;

    private final HaMasterSelectSnapshotRep snapshotRep;

    private final ScheduledExecutorService renewExecutor;

    private final TransactionTemplate txTemplate;

    private final MasterInitListener masterInitListener;

    private final ExecutorService selectExecutor;

    private final HaServiceInstanceService instanceService;

    public HaMasterSelectService(HaMasterRep haMasterRep,
                                 HaMasterSelectSnapshotRep snapshotRep,
                                 TransactionTemplate txTemplate,
                                 MasterInitListener masterInitListener,
                                 HaServiceInstanceService instanceService) {
        this.haMasterRep = haMasterRep;
        this.snapshotRep = snapshotRep;
        this.renewExecutor = Executors.newSingleThreadScheduledExecutor();
        this.txTemplate = txTemplate;
        this.masterInitListener = masterInitListener;
        this.selectExecutor = Executors.newSingleThreadExecutor();
        this.instanceService = instanceService;
    }

    @Override
    public void destroy() throws DestroyFailedException {
        this.renewExecutor.shutdownNow();
        this.selectExecutor.shutdownNow();
    }

    public Future<?> asyncServeAsMaster(HaMasterSelectReq req) {
        return selectExecutor.submit(() -> serveAsMaster(req));
    }

    public void serveAsMaster(HaMasterSelectReq req) {
        try {
            txTemplate.executeWithoutResult(status -> {
                selectMaster(req);
                saveSnapshot(req);
                instanceService.becomeMaster(req.getServiceName(), req.getHost(), req.getPort());
            });

            // 开启续约
            scheduleRenew(req);
            // 监听初始化完成事件
            listen(req);
        } catch (Exception e) {
            if (e instanceof DuplicateKeyException) {
                upgradeMaster(req);
            } else {
                log.warn("Serve as HA master selection instance fail: ", e);
                throw new BusinessException("HA_MASTER_SELECTION_FAIL",
                        "Serve as HA master selection instance fail, error: " + e.getMessage());
            }
        }
    }

    private void upgradeMaster(HaMasterSelectReq req) {
        while (true) {
            HaMasterInstance currentMasterInstance = haMasterRep.getOne(Wrappers.<HaMasterInstance>lambdaQuery()
                    .eq(HaMasterInstance::getServiceName, req.getServiceName()));
            if (currentMasterInstance == null) {
                serveAsMaster(req);
                return;
            }

            AtomicBoolean upgrade = new AtomicBoolean(false);
            txTemplate.executeWithoutResult(status -> {
                if (upgrade(req)) {
                    upgrade.set(true);
                    saveSnapshot(req);
                    instanceService.becomeMaster(req.getServiceName(), req.getHost(), req.getPort());
                }
            });
            // 成功升级成 master
            if (upgrade.get()) {
                // 开启续约
                scheduleRenew(req);
                // 监听初始化完成事件
                listen(req);
                return;
            }

            LockSupport.parkNanos(1000 * 1000 * 100); // 100ms
        }
    }

    private void listen(HaMasterSelectReq req) {
        log.info("HA master selection success and then listen, req: {}", req);
        Object initEvent = masterInitListener.subscribe();
        log.info("HA master selection listen success, req: {}", req);

        if (initEvent == null || !start(req)) {
            cancelRenew();
        }
    }

    private void saveSnapshot(HaMasterSelectReq req) {
        log.info("Serve as HA master selection instance success: {}", req);
        snapshotRep.save(HaMasterSelectSnapshot.builder()
                .instanceInfo(JSON.toJSONString(HaMasterInstance.builder()
                        .serviceName(req.getServiceName())
                        .host(req.getHost())
                        .port(req.getPort())
                        .build()))
                .serviceName(req.getServiceName())
                .createTime(new Date())
                .build());
    }

    private void selectMaster(HaMasterSelectReq req) {
        HaMasterInstance instance = BeanConvertor.convert(req);
        instance.setEpoch(0);
        haMasterRep.save(instance);
    }

    //TODO 如果下线需要剔除续租任务，避免影响后续其他实例选主
    private void scheduleRenew(HaMasterSelectReq req) {
        renewExecutor.scheduleAtFixedRate(() -> renew(req), req.getRenewIntervalSeconds(),
                req.getRenewIntervalSeconds(), TimeUnit.SECONDS);
    }

    private void renew(HaMasterSelectReq req) {
        haMasterRep.update(HaMasterInstance.builder()
                        .expireAt(System.currentTimeMillis() + req.getRenewIntervalSeconds() * 1000 * 3)
                        .build(),
                Wrappers.<HaMasterInstance>lambdaUpdate()
                        .eq(HaMasterInstance::getServiceName, req.getServiceName())
                        .eq(HaMasterInstance::getHost, req.getHost())
                        .eq(HaMasterInstance::getPort, req.getPort()));
    }

    private void cancelRenew() {
        renewExecutor.shutdownNow();
    }

    private boolean upgrade(HaMasterSelectReq req) {
        return haMasterRep.update(BeanConvertor.convert(req), Wrappers.<HaMasterInstance>lambdaUpdate()
                .setSql(true, "epoch = epoch + 1")
                .eq(HaMasterInstance::getServiceName, req.getServiceName())
                .le(HaMasterInstance::getExpireAt, System.currentTimeMillis()));
    }

    private boolean start(HaMasterSelectReq req) {
        HaMasterInstance instance = HaMasterInstance.builder().instanceStatus(HaMasterSelectInstanceStatusEnum.SERVING.getCode()).build();
        return haMasterRep.update(instance, Wrappers.<HaMasterInstance>lambdaUpdate()
                .eq(HaMasterInstance::getServiceName, req.getServiceName())
                .eq(HaMasterInstance::getHost, req.getHost())
                .eq(HaMasterInstance::getPort, req.getPort())
                .eq(HaMasterInstance::getInstanceStatus, HaMasterSelectInstanceStatusEnum.STARTING.getCode()));
    }

    @Override
    public void close() throws Exception {
        this.destroy();
    }
}
