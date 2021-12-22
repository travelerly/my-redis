package com.colin.redis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author colin
 * @create 2021-12-20 20:17
 */
@Api(description = "案例实战:天猫亿级UV的Redis统计方案")
@RestController
@Slf4j
public class HyperLogLogController {

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation("获得IP去重后的首页访问量")
    @RequestMapping(value = "/uv",method = RequestMethod.GET)
    public long uv() {
        //pfcount
        return redisTemplate.opsForHyperLogLog().size("hll");
    }
}
