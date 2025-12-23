package com.wly.center.admin.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum HaInstanceStatusEnum {
    OFFLINE(0),
    ONLINE(1);

    private final Integer code;
}
