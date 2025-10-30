package com.wly.center.admin.manager;

import com.wly.center.admin.dao.entity.Node;

import java.util.List;

public interface NodeStorage {
    Boolean add(Node node);

    Node get(Long id);

    Node get(String name);

    List<Node> children(Long parentId);

    List<Node> children(String parentName);

    Node remove(Long id);

    Node remove(String name);

    void update(Node node);
}
