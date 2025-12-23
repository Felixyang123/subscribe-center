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
@TableName(value = "ha_master_instance", autoResultMap = true)
public class HaMasterInstance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String serviceName;

    private String host;

    private Integer port;

    /**
     * 0-starting 1-serving
     * @see com.wly.center.admin.enumeration.HaMasterSelectInstanceStatusEnum
     */
    private Integer instanceStatus;

    private Integer epoch;

    private Long expireAt;

    private Date createTime;

    private Date updateTime;
}
