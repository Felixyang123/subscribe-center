package com.wly.center.admin.controller.open;

import com.wly.center.admin.service.WatcherService;
import com.wly.center.core.pojo.Result;
import com.wly.center.core.pojo.req.OpenWatchReq;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 监听器控制-open api
 *
 * @param watcherService
 */
@RestController
@RequestMapping("/open/watcher")
public record OpenWatcherController(WatcherService watcherService) {

    /**
     * 监听节点
     *
     * @param req
     * @return
     */
    @PostMapping("/watch")
    public Result<Void> watch(@RequestBody OpenWatchReq req) {
        watcherService.watch(req);
        return Result.success();
    }

}
