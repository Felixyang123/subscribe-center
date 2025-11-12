package com.wly.center.samples.controller;

import com.wly.center.core.pojo.Result;
import com.wly.center.samples.service.ConfDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 配置示例
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/demo/conf")
public class ConfDemoController {
    private final ConfDemoService confDemoService;

    /**
     * 获取配置
     *
     * @param key
     * @return
     */
    @GetMapping("/get")
    public Result<Object> get(@RequestParam("key") String key) {
        return Result.success(confDemoService.conf(key));
    }

    /**
     * 获取配置列表
     *
     * @return
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list() {
        return Result.success(confDemoService.confList());
    }

    /**
     * 更新配置
     *
     * @param conf
     * @return
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody Map<String, Object> conf) {
        confDemoService.update(conf);
        return Result.success();
    }
}
