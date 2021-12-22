package com.colin.redis.controller;

import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author colin
 * @create 2021-06-08 09:18
 */
@RestController
@Slf4j
public class RedLockController {

    public static final String CACHE_KEY_REDLOCK = "COLIN_REDLOCK";

    @Autowired
    RedissonClient redissonClient1;

    @Autowired
    RedissonClient redissonClient2;

    @Autowired
    RedissonClient redissonClient3;

    @GetMapping(value = "/redlock")
    public void getlock() {
        //CACHE_KEY_REDLOCK为redis 分布式锁的key
        RLock lock1 = redissonClient1.getLock(CACHE_KEY_REDLOCK);
        RLock lock2 = redissonClient2.getLock(CACHE_KEY_REDLOCK);
        RLock lock3 = redissonClient3.getLock(CACHE_KEY_REDLOCK);

        RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);
        boolean isLock;

        try {

            // waitTime 锁的等待时间处理,正常情况下 等5s
            // leaseTime就是redis key的过期时间,正常情况下等5分钟。
            isLock = redLock.tryLock(5, 300, TimeUnit.SECONDS);
            log.info("线程{}，是否拿到锁：{} ",Thread.currentThread().getName(),isLock);
            if (isLock) {
                //TODO if get lock success, do something;
                //暂停20秒钟线程
                try { TimeUnit.SECONDS.sleep(60); } catch (InterruptedException e) { e.printStackTrace(); }
            }

        } catch (Exception e) {
            log.error("redlock exception ",e);
        } finally {
            // 无论如何, 最后都要解锁
            redLock.unlock();
            System.out.println(Thread.currentThread().getName()+"\t"+"redLock.unlock()");
        }
    }

}
