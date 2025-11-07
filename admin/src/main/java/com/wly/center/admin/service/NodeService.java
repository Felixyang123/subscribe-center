package com.wly.center.admin.service;

import com.wly.center.admin.common.BeanConvertor;
import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.lock.LockTemplate;
import com.wly.center.admin.manager.NodeManager;
import com.wly.center.core.enumeration.NodeTypeEnum;
import com.wly.center.core.exception.NodeNotExistException;
import com.wly.center.core.pojo.req.OpenAddNodeReq;
import com.wly.center.core.pojo.req.OpenRenewNodeReq;
import com.wly.center.core.pojo.req.OpenUpdateNodeReq;
import com.wly.center.core.pojo.resp.OpenAddNodeResp;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public record NodeService(NodeManager nodeManager, LockTemplate lockTemplate) {

    public OpenAddNodeResp addNode(OpenAddNodeReq req) {
        if (Boolean.TRUE.equals(req.getSlave())) {
            Date now = new Date();
            Node masterNode = Node.builder()
                    .parentId(0L)
                    .name(req.getName())
                    .expireAt(-1L)
                    .type(NodeTypeEnum.MASTER.getCode())
                    .createTime(now)
                    .build();

            try {
                nodeManager.add(masterNode);
            } catch (Exception e) {
                log.warn("Failed to add master node: {}", e.getMessage());
                masterNode = nodeManager.nodeStorage().get(masterNode.getName());
            }

            String uniqueId = UUID.randomUUID().toString().replace("-", "");
            Node slaveNode = Node.builder()
                    .name(req.getName() + ":" + uniqueId)
                    .data(req.getData())
                    .type(NodeTypeEnum.SLAVE.getCode())
                    .parentId(masterNode.getId())
                    .createTime(now)
                    .build();

            if (Boolean.TRUE.equals(req.getTemporary())) {
                slaveNode.setExpireAt(req.getExpireAt());
            }

            nodeManager.add(slaveNode);

            return OpenAddNodeResp.builder()
                    .masterNode(BeanConvertor.convert(masterNode))
                    .slaveNode(BeanConvertor.convert(slaveNode))
                    .build();
        } else {
            Node node = Node.builder()
                    .parentId(0L)
                    .name(req.getName())
                    .data(req.getData())
                    .type(NodeTypeEnum.MASTER.getCode())
                    .createTime(new Date())
                    .build();

            if (Boolean.TRUE.equals(req.getTemporary())) {
                node.setExpireAt(req.getExpireAt());
            }

            nodeManager.add(node);

            return OpenAddNodeResp.builder().masterNode(BeanConvertor.convert(node)).build();
        }
    }

    public void removeNode(String name) {
        lockTemplate.lockThenExecute(name, () -> {
            Node node = nodeManager.nodeStorage().get(name);
            log.debug("Remove node: {}-{}", name, node);
            if (node != null) {
                nodeManager.remove(node);
            }
        });
    }

    public void updateNode(OpenUpdateNodeReq req) {
        Node node = nodeManager.nodeStorage().get(req.getName());
        if (node == null) {
            throw new NodeNotExistException("节点不存在：" + req.getName());
        }

        node.setData(req.getData());
        nodeManager.update(node);
    }

    public OpenNodeDetailResp detail(String nodeName) {
        return Optional.ofNullable(nodeManager.nodeStorage().get(nodeName)).map(BeanConvertor::convert).orElse(null);
    }

    public List<OpenNodeDetailResp> children(String parentName) {
        Node parentNode = nodeManager.nodeStorage().get(parentName);
        if (parentNode == null) {
            throw new NodeNotExistException("节点不存在：" + parentName);
        }

        return nodeManager.nodeStorage().children(parentNode.getId()).stream().map(BeanConvertor::convert).toList();
    }

    public void renew(OpenRenewNodeReq req) {
        Node node = nodeManager.nodeStorage().get(req.getNodeName());
        if (node == null) {
            throw new NodeNotExistException("节点不存在：" + req.getNodeName());
        }

        node.setExpireAt(req.getExpireAt());
        nodeManager.nodeStorage().update(node);
    }
}
