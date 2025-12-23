package com.wly.center.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "ha_service_instance", autoResultMap = true)
public class HaServiceInstance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String serviceName;

    private String host;

    private Integer port;

    private Boolean serveAsMaster;

    private Long expireAt;

    private Long lastHeartbeat;

    /**
     * 0-offline 1-online
     * @see com.wly.center.admin.enumeration.HaInstanceStatusEnum
     */
    private Integer instanceStatus;

    private String metadata;

    private Date createdAt;

    private Date updatedAt;
}
