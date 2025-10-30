package com.wly.center.core.pojo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenRenewNodeReq {

    private String nodeName;

    private Long expireAt;
}
