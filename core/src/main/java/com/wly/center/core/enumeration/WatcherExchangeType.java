package com.wly.center.core.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WatcherExchangeType {
    NODE_DELETE(0),
    DATA_CHANGE(1),
    CHILDREN_LIST_CHANGE(2);
    private final Integer code;
}
