package com.wly.center.core.pojo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenHaClusterInfoResp implements Serializable {
    @Serial
    private static final long serialVersionUID = -2769035722677002049L;

    private HaInstanceDto master;

    private List<HaInstanceDto> salves;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HaInstanceDto implements Serializable {
        @Serial
        private static final long serialVersionUID = 5625381380258695240L;

        private String serviceName;

        private String host;

        private Integer port;

    }
}
