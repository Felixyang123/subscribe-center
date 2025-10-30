package com.wly.center.admin.common;

import com.wly.center.admin.dao.entity.Node;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;

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

}
