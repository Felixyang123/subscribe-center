package com.wly.center.core.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NodeTypeEnum {
    MASTER(0, "主节点"),
    SLAVE(1, "从节点");
    private final Integer code;

    private final String description;
}
