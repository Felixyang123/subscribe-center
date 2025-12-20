package com.wly.center.admin.controller.open;

import com.wly.center.admin.service.HaServiceInstanceService;
import com.wly.center.core.pojo.Result;
import com.wly.center.core.pojo.resp.OpenHaClusterInfoResp;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open/ha")
@RequiredArgsConstructor
public class OpenHaController {

    private final HaServiceInstanceService instanceService;

    /**
     * 获取集群信息
     * @param serviceName
     * @return
     */
    @GetMapping("/cluster")
    public Result<OpenHaClusterInfoResp> queryClusterInfo(@RequestParam("serviceName") String serviceName) {
        return Result.success(instanceService.queryClusterInfo(serviceName));
    }

    /**
     * 探活
     * @return
     */
    @GetMapping("/probe")
    public Result<String> probe() {
        return Result.success("OK");
    }

}
