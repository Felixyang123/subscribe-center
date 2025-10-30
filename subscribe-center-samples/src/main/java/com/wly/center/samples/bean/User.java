package com.wly.center.samples.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String name;

    private Integer age;

    private Integer sex;

    private String address;

    private String phone;
}
