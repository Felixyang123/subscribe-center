package com.wly.center.core.pojo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAddNodeResp {

    private OpenNodeDetailResp masterNode;

    private OpenNodeDetailResp slaveNode;
}
