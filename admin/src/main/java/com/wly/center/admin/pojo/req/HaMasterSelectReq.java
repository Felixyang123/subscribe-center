package com.wly.center.admin.pojo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HaMasterSelectReq {

    private String serviceName;

    private String host;

    private Integer port;

    private Long renewIntervalSeconds;
}
