package com.wly.center.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wly.center.admin.dao.entity.HaServiceInstance;
import com.wly.center.admin.dao.rep.HaServiceInstanceRep;
import com.wly.center.admin.enumeration.HaInstanceStatusEnum;
import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.exception.BusinessExceptions;
import com.wly.center.core.pojo.resp.OpenHaClusterInfoResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HaServiceInstanceService {

    private final HaServiceInstanceRep instanceRep;

    public void becomeMaster(String serviceName, String host, Integer port) {
        List<HaServiceInstance> instances = instanceRep.list(Wrappers.<HaServiceInstance>lambdaQuery()
                .eq(HaServiceInstance::getServiceName, serviceName));
        Optional<HaServiceInstance> masterInstanceOptional = instances.stream().filter(instance ->
                Objects.equals(host, instance.getHost()) && Objects.equals(port, instance.getPort())).findFirst();

        masterInstanceOptional.ifPresentOrElse(masterInstance -> {
            for (HaServiceInstance instance : instances) {
                if (instance.getId().equals(masterInstance.getId())) {
                    instance.setServeAsMaster(Boolean.TRUE);
                } else {
                    instance.setServeAsMaster(Boolean.FALSE);
                }
            }
            instanceRep.updateBatchById(instances);
        }, () -> {
            throw new BusinessException(BusinessExceptions.INSTANCE_NOT_EXIST.name(),
                    String.format("%s:%s:%s become master fail, instance not exists", serviceName, host, port));
        });
    }

    public OpenHaClusterInfoResp queryClusterInfo(String serviceName) {
        List<HaServiceInstance> availableInstances = instanceRep.list(Wrappers.<HaServiceInstance>lambdaQuery()
                .eq(HaServiceInstance::getServiceName, serviceName)
                .eq(HaServiceInstance::getInstanceStatus, HaInstanceStatusEnum.ONLINE.getCode())
                .gt(HaServiceInstance::getExpireAt, System.currentTimeMillis()));

        OpenHaClusterInfoResp clusterInfoResp = new OpenHaClusterInfoResp();
        clusterInfoResp.setSalves(new ArrayList<>());

        for (HaServiceInstance availableInstance : availableInstances) {
            if (Boolean.TRUE.equals(availableInstance.getServeAsMaster())) {
                clusterInfoResp.setMaster(OpenHaClusterInfoResp.HaInstanceDto.builder()
                        .serviceName(availableInstance.getServiceName())
                        .host(availableInstance.getHost())
                        .port(availableInstance.getPort())
                        .build());
            } else {
                clusterInfoResp.getSalves().add(OpenHaClusterInfoResp.HaInstanceDto.builder()
                        .serviceName(availableInstance.getServiceName())
                        .host(availableInstance.getHost()).port(availableInstance.getPort())
                        .build()
                );
            }
        }
        return clusterInfoResp;
    }
}
