package com.colin.redis.service;

import cn.hutool.core.date.DateUtil;
import com.colin.redis.entities.Product;
import com.colin.redis.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author colin
 * @create 2021-05-24 09:08
 */
@Service
@Slf4j
public class JHSTaskService {

    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void initJHS(){
        log .info( " 启动定时器淘宝聚划算功能模拟 .........." + DateUtil. now ());
        new Thread(()->{
            // 模拟定时器，定时把数据库的特价商品，刷新到 redis 中
            while (true){
                // 模拟从数据库读取 100 件特价商品，用于加载到聚划算的页面中
                List<Product> list = this.products();

                // 先更新 B 缓存
                this.redisTemplate.delete(Constants.JHS_KEY_B);
                this.redisTemplate.opsForList().leftPushAll(Constants.JHS_KEY_B,list);
                this.redisTemplate.expire(Constants.JHS_KEY_B,20L,TimeUnit.DAYS);

                //再更新 A 缓存
                this.redisTemplate.delete(Constants.JHS_KEY_A);
                this.redisTemplate.opsForList().leftPushAll(Constants.JHS_KEY_A,list);
                this.redisTemplate.expire(Constants.JHS_KEY_A,15L,TimeUnit.DAYS);

                // 间隔一分钟 执行一遍
                try {TimeUnit.MINUTES.sleep(1);} catch (InterruptedException e) {e.printStackTrace();}
                log.info("runJhs 定时刷新 ..............");
            }
        },"thread-A").start();
    }

    /**
     * 模拟从数据库读取 100 件特价商品，用于加载到聚划算的页面中
     */
    public List<Product> products(){
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Random random = new Random();
            int id = random.nextInt(10000);
            Product product= new Product(( long ) id,"product" +i,i,"detail");
            list.add(product);
        }
        return list;
    }
}
