package com.wly.center.core.pojo;

import lombok.Data;

@Data
public class PageReq<T> {
    private Integer pageNum;
    private Integer pageSize;

    private T query;
}
