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
@TableName(value = "node", autoResultMap = true)
public class Node {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String name;

    /**
     * @see com.wly.center.core.enumeration.NodeTypeEnum
     */
    private Integer type;

    private String data;

    private Long expireAt;

    private Date createTime;
}
