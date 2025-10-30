package com.wly.center.admin.controller.open;

import com.wly.center.admin.service.NodeService;
import com.wly.center.core.pojo.Result;
import com.wly.center.core.pojo.req.OpenAddNodeReq;
import com.wly.center.core.pojo.req.OpenRenewNodeReq;
import com.wly.center.core.pojo.req.OpenUpdateNodeReq;
import com.wly.center.core.pojo.resp.OpenAddNodeResp;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wly
 * @date 2021/11/23
 * 节点控制-open api
 */
@RestController
@RequestMapping("/open/node")
public record OpenNodeController(NodeService nodeService) {

    /**
     * 创建节点
     *
     * @param req
     * @return
     */
    @PostMapping("/add")
    public Result<OpenAddNodeResp> addNode(@RequestBody OpenAddNodeReq req) {
        return Result.success(nodeService.addNode(req));
    }


    /**
     * 删除节点
     *
     * @param nodeName
     * @return
     */
    @PostMapping("/remove")
    public Result<Void> removeNode(@RequestParam(value = "nodeName") String nodeName) {
        nodeService.removeNode(nodeName);
        return Result.success();
    }

    /**
     * 修改节点
     *
     * @param req
     * @return
     */
    @PostMapping("/update")
    public Result<Void> updateNode(@RequestBody OpenUpdateNodeReq req) {
        nodeService.updateNode(req);
        return Result.success();
    }

    /**
     * 节点详情
     *
     * @param nodeName
     * @return
     */
    @GetMapping("/detail")
    public Result<OpenNodeDetailResp> detail(@RequestParam(value = "nodeName") String nodeName) {
        return Result.success(nodeService.detail(nodeName));
    }

    /**
     * 获取子节点
     *
     * @param parentName
     * @return
     */
    @GetMapping("/children")
    public Result<List<OpenNodeDetailResp>> children(@RequestParam(value = "parentName") String parentName) {
        return Result.success(nodeService.children(parentName));
    }

    /**
     * 续约节点
     *
     * @param req
     * @return
     */
    @PostMapping("/renew")
    public Result<Void> renew(@RequestBody OpenRenewNodeReq req) {
        nodeService.renew(req);
        return Result.success();
    }
}
