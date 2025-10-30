package com.wly.center.core.protocal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatcherExchangeReq {

    private String node;

    /**
     * 0-node delete 1-node data change
     * @see com.wly.center.core.enumeration.WatcherExchangeType
     */
    private Integer exchangeType;
}
