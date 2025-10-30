package com.wly.center.core.protocal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequest {

    private String requestId;

    /**
     * 1:watcherExchange
     * @see com.wly.center.core.enumeration.ExchangeType
     */
    private Integer type;

    private Object req;

    private Long executionTime;

    private String ip;

    private Integer port;
}
