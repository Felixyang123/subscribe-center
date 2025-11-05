package com.wly.center.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wly.center.core.enumeration.WatcherCycleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "watcher", autoResultMap = true)
public class Watcher {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "`key`")
    private String key;

    private String node;

    private String ip;

    private Integer port;

    /**
     * 0-循环 1-单次
     * @see WatcherCycleEnum
     */
    private Integer cycleType;

    private Date createTime;
}
