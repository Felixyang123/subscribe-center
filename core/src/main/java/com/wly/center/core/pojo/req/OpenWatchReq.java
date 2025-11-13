package com.wly.center.core.pojo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenWatchReq {

    private String key;

    private String nodeName;

    private String ip;

    private Integer port;

    /**
     * 0-循环 1-单次
     *
     * @see com.wly.center.core.enumeration.WatcherCycleEnum
     */
    private Integer cycleType;

    /**
     * 0-节点删除 1-节点数据变更 2-节点子节点列表变更
     *
     * @see com.wly.center.core.enumeration.WatcherExchangeType
     */
    private Integer notifyType;
}
