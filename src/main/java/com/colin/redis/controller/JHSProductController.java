package com.colin.redis.controller;

import com.colin.redis.entities.Product;
import com.colin.redis.util.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author colin
 * @create 2021-05-24 09:19
 */
@RestController
@Slf4j
@Api(description="聚划算商品列表接口")
public class JHSProductController {

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/pruduct/findab" ,method = RequestMethod.GET)
    @ApiOperation("按照分页和每页显示容量，点击查看 AB")
    public List<Product> find(int page,int size){
        List<Product> list = null;
        long start = (page - 1 ) * size;
        long end = start + size - 1 ;
        
        try {
            // 采用 redis list 数据结构的 lrange 命令实现分页查询
            list = this.redisTemplate.opsForList().range(Constants.JHS_KEY_A, start, end);
            if (CollectionUtils.isEmpty(list)){
                log.info("=========A 缓存已经失效了，记得人工修补，B 缓存自动延续 5 天 " );
                // 用户先查询缓存 A(上面的代码)，如果缓存 A 查询不到（例如，更新缓存的时候删除了），再查询缓存 B
                this.redisTemplate.opsForList().range(Constants.JHS_KEY_B, start, end);
            }
            log.info("查询结果：{}",list);
        }catch (Exception e){
            // 这里的异常，一般是 redis 瘫痪 ，或 redis 网络 timeout
            log.error("exception:" , e);
            // TODO 走 DB 查询
        }
        return list;
    }
}
