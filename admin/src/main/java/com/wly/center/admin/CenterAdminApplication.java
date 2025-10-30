package com.wly.center.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wly.center.admin.dao.mapper")
public class CenterAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(CenterAdminApplication.class, args);
    }
}
