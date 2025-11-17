package com.wly.center.admin.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HaMasterSelectInstanceStatusEnum {
    STARTING(0),
    SERVING(1);

    private final Integer code;
}
