package com.wly.center.samples.service;

import com.wly.center.core.enumeration.NodeTypeEnum;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.core.pojo.req.OpenAddNodeReq;
import com.wly.center.core.pojo.resp.OpenAddNodeResp;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootTest
public class NodeOptTest {

    @Autowired
    private ExchangeServerFactory serverFactory;

    @Test
    void createMasterNodeTest() {
        String name = UUID.randomUUID().toString().replace("-", "");
        long exp = System.currentTimeMillis() + (1000 * 60 * 60);
        OpenAddNodeResp resp = serverFactory.defaultExchangeClient().addNode(
                OpenAddNodeReq.builder()
                        .name(name)
                        .data("master data")
                        .expireAt(exp)
                        .build());
        Assertions.assertTrue(resp != null && resp.getMasterNode() != null && resp.getSlaveNode() == null);

        OpenNodeDetailResp masterNode = resp.getMasterNode();
        Assertions.assertTrue(masterNode.getId() != null && masterNode.getId() > 0);
        Assertions.assertEquals(name, masterNode.getName());
        Assertions.assertEquals("master data", masterNode.getData());
        Assertions.assertEquals(exp, masterNode.getExpireAt());
        Assertions.assertEquals(NodeTypeEnum.MASTER.getCode(), masterNode.getType());

    }

    @Test
    void createSlaveNodeTest() {
        String masterName = UUID.randomUUID().toString().replace("-", "");
        long exp = System.currentTimeMillis() + (1000 * 60 * 60);
        OpenAddNodeResp resp = serverFactory.defaultExchangeClient().addNode(
                OpenAddNodeReq.builder()
                        .name(masterName)
                        .data("slave data")
                        .expireAt(exp)
                        .slave(Boolean.TRUE)
                        .build());
        Assertions.assertTrue(resp != null && resp.getMasterNode() != null && resp.getSlaveNode() != null);

        OpenNodeDetailResp masterNode = resp.getMasterNode();
        Assertions.assertTrue(masterNode.getId() != null && masterNode.getId() > 0);
        Assertions.assertEquals(masterName, masterNode.getName());
        Assertions.assertNull(masterNode.getData());
        Assertions.assertEquals(-1L, masterNode.getExpireAt());
        Assertions.assertEquals(NodeTypeEnum.MASTER.getCode(), masterNode.getType());

        OpenNodeDetailResp slaveNode = resp.getSlaveNode();
        Assertions.assertTrue(slaveNode.getId() != null && slaveNode.getId() > 0);
        Assertions.assertNotNull(slaveNode.getName());
        Assertions.assertEquals("slave data", slaveNode.getData());
        Assertions.assertEquals(exp, slaveNode.getExpireAt());
        Assertions.assertEquals(NodeTypeEnum.SLAVE.getCode(), slaveNode.getType());
    }

    @Test
    void createNodeChildrenTest() {
        String masterName = UUID.randomUUID().toString().replace("-", "");
        long exp = System.currentTimeMillis() + (1000 * 60 * 60);
        OpenAddNodeResp resp = serverFactory.defaultExchangeClient().addNode(
                OpenAddNodeReq.builder()
                        .name(masterName)
                        .data("slave data 1")
                        .expireAt(exp)
                        .slave(Boolean.TRUE)
                        .build());
        Assertions.assertTrue(resp != null && resp.getMasterNode() != null && resp.getSlaveNode() != null);

        OpenNodeDetailResp masterNode = resp.getMasterNode();
        Assertions.assertTrue(masterNode.getId() != null && masterNode.getId() > 0);
        Assertions.assertEquals(masterName, masterNode.getName());
        Assertions.assertNull(masterNode.getData());
        Assertions.assertEquals(-1L, masterNode.getExpireAt());
        Assertions.assertEquals(NodeTypeEnum.MASTER.getCode(), masterNode.getType());

        OpenNodeDetailResp slaveNode = resp.getSlaveNode();
        Assertions.assertTrue(slaveNode.getId() != null && slaveNode.getId() > 0);
        Assertions.assertNotNull(slaveNode.getName());
        Assertions.assertEquals("slave data 1", slaveNode.getData());
        Assertions.assertEquals(exp, slaveNode.getExpireAt());
        Assertions.assertEquals(NodeTypeEnum.SLAVE.getCode(), slaveNode.getType());
        Assertions.assertEquals(masterNode.getId(), slaveNode.getParentId());

        resp = serverFactory.defaultExchangeClient().addNode(
                OpenAddNodeReq.builder()
                        .name(masterName)
                        .data("slave data 2")
                        .expireAt(exp)
                        .slave(Boolean.TRUE)
                        .build());
        Assertions.assertTrue(resp != null && resp.getMasterNode() != null && resp.getSlaveNode() != null);

        masterNode = resp.getMasterNode();
        Assertions.assertTrue(masterNode.getId() != null && masterNode.getId() > 0);
        Assertions.assertEquals(masterName, masterNode.getName());
        Assertions.assertNull(masterNode.getData());
        Assertions.assertEquals(-1L, masterNode.getExpireAt());
        Assertions.assertEquals(NodeTypeEnum.MASTER.getCode(), masterNode.getType());

        OpenNodeDetailResp slaveNode1 = resp.getSlaveNode();
        Assertions.assertTrue(slaveNode1.getId() != null && slaveNode1.getId() > 0);
        Assertions.assertNotNull(slaveNode1.getName());
        Assertions.assertEquals("slave data 2", slaveNode1.getData());
        Assertions.assertEquals(exp, slaveNode1.getExpireAt());
        Assertions.assertEquals(NodeTypeEnum.SLAVE.getCode(), slaveNode1.getType());
        Assertions.assertEquals(masterNode.getId(), slaveNode1.getParentId());
        Assertions.assertNotEquals(slaveNode.getId(), slaveNode1.getId());

        resp = serverFactory.defaultExchangeClient().addNode(
                OpenAddNodeReq.builder()
                        .name(masterName)
                        .data("slave data 3")
                        .expireAt(exp)
                        .slave(Boolean.TRUE)
                        .build());
        Assertions.assertTrue(resp != null && resp.getMasterNode() != null && resp.getSlaveNode() != null);

        masterNode = resp.getMasterNode();
        Assertions.assertTrue(masterNode.getId() != null && masterNode.getId() > 0);
        Assertions.assertEquals(masterName, masterNode.getName());
        Assertions.assertNull(masterNode.getData());
        Assertions.assertEquals(-1L, masterNode.getExpireAt());
        Assertions.assertEquals(NodeTypeEnum.MASTER.getCode(), masterNode.getType());

        OpenNodeDetailResp slaveNode2 = resp.getSlaveNode();
        Assertions.assertTrue(slaveNode2.getId() != null && slaveNode2.getId() > 0);
        Assertions.assertNotNull(slaveNode2.getName());
        Assertions.assertEquals("slave data 3", slaveNode2.getData());
        Assertions.assertEquals(exp, slaveNode2.getExpireAt());
        Assertions.assertEquals(NodeTypeEnum.SLAVE.getCode(), slaveNode2.getType());
        Assertions.assertEquals(masterNode.getId(), slaveNode2.getParentId());
        Assertions.assertNotEquals(slaveNode1.getId(), slaveNode2.getId());

        List<OpenNodeDetailResp> children = serverFactory.defaultExchangeClient().children(masterName);
        Map<Long, OpenNodeDetailResp> childMap = children.stream().collect(Collectors.toMap(OpenNodeDetailResp::getId, Function.identity()));

        OpenNodeDetailResp child = childMap.get(slaveNode.getId());
        Assertions.assertEquals(slaveNode, child);

        OpenNodeDetailResp child1 = childMap.get(slaveNode1.getId());
        Assertions.assertEquals(slaveNode1, child1);

        OpenNodeDetailResp child2 = childMap.get(slaveNode2.getId());
        Assertions.assertEquals(slaveNode2, child2);
    }



}
