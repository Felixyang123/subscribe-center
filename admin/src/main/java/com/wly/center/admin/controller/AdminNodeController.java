package com.wly.center.admin.controller;

import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.service.NodeService;
import com.wly.center.core.exception.NodeNotExistException;
import com.wly.center.core.pojo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 节点控制-admin api
 * @param nodeService
 */
@RestController
@RequestMapping("/admin/node")
public record AdminNodeController(NodeService nodeService) {

    /**
     * 删除节点
     *
     * @param id
     * @return
     */
    @PostMapping("/remove")
    public Result<Void> remove(@RequestParam("id") Long id) {
        Node node = nodeService.nodeManager().nodeStorage().get(id);
        if (node == null) {
            throw new NodeNotExistException("节点不存在：" + id);
        }

        nodeService.nodeManager().remove(node);
        return Result.success();
    }
}
