package com.wly.center.core.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WatcherCycleEnum {
    CYCLE(0, "循环"),
    SINGLE(1, "单次");

    private final Integer code;
    private final String description;
}
