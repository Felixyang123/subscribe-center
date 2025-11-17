package com.wly.center.admin.common;

import com.wly.center.admin.dao.entity.HaMasterInstance;
import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.enumeration.HaMasterSelectInstanceStatusEnum;
import com.wly.center.admin.pojo.req.HaMasterSelectReq;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;

import java.util.Date;

public class BeanConvertor {

    public static OpenNodeDetailResp convert(Node node) {
        return OpenNodeDetailResp.builder()
                .id(node.getId())
                .parentId(node.getParentId())
                .name(node.getName())
                .type(node.getType())
                .data(node.getData())
                .expireAt(node.getExpireAt())
                .createTime(node.getCreateTime())
                .build();
    }

    public static HaMasterInstance convert(HaMasterSelectReq req) {
        Date date = new Date();
        return HaMasterInstance.builder()
                .serviceName(req.getServiceName())
                .host(req.getHost())
                .port(req.getPort())
                .instanceStatus(HaMasterSelectInstanceStatusEnum.STARTING.getCode())
                .expireAt(System.currentTimeMillis() + req.getRenewIntervalSeconds() * 1000 * 3)
                .createTime(date)
                .updateTime(date)
                .build();
    }

}
