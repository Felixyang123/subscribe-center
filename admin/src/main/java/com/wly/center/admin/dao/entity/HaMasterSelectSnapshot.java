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
@TableName(value = "ha_master_select_snapshot", autoResultMap = true)
public class HaMasterSelectSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String serviceName;

    /**
     * HaMasterInstance json
     */
    private String instanceInfo;

    private String operator;

    private Date createTime;
}
