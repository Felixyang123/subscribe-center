package com.wly.center.admin.dao.rep;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wly.center.admin.dao.entity.Node;
import com.wly.center.admin.dao.mapper.NodeMapper;
import org.springframework.stereotype.Repository;

@Repository
public class NodeRep extends ServiceImpl<NodeMapper, Node> {
}
