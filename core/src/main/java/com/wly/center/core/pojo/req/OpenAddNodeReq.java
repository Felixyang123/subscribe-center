package com.wly.center.core.pojo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAddNodeReq {

    private String name;

    private String data;

    private Long expireAt;

    private Boolean slave;

    private Boolean temporary;
}
