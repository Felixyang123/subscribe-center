package com.wly.center.core.protocal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeResponse {

    private String requestId;

    private Object result;

    private Boolean success;

    private String errorMsg;
}
