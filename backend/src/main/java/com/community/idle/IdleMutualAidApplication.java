package com.community.idle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.community.idle.mapper")
public class IdleMutualAidApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdleMutualAidApplication.class, args);
    }
}
