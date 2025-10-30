package com.wly.center.core.pojo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenNodeDetailResp {

    private Long id;

    private Long parentId;

    private String name;

    /**
     * 1:master 0:slave
     * @see com.wly.center.core.enumeration.NodeTypeEnum
     */
    private Integer type;

    private String data;

    private Long expireAt;

    private Date createTime;
}
