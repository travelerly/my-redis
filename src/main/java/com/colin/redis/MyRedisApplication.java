package com.colin.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.colin.redis.mapper")
public class MyRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyRedisApplication.class, args);
    }

}
