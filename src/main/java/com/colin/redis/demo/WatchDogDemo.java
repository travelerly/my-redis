package com.colin.redis.demo;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

/**
 * @author colin
 * @create 2021-12-21 14:31
 */
public class WatchDogDemo {

    public static final String LOCKKEY = "colin_key";
    private static Config config;
    private static Redisson redisson;

    static {
        config = new Config();
        config.useSingleServer().setAddress("redis://"+"127.0.0.1"+":6379").setDatabase(0);
        redisson = (Redisson)Redisson.create(config);
    }

    public static void main(String[] args) {

        RLock redissonLock = redisson.getLock(LOCKKEY);
        redissonLock.lock();
        // 多次加锁，验证锁的可重入性，但要保证加锁与解锁成对出现
        /*redissonLock.lock();
        redissonLock.lock();*/

        try {
            System.out.println("1111");
            // 暂停几秒钟线程
            try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (redissonLock.isLocked() && redissonLock.isHeldByCurrentThread()){
                redissonLock.unlock();
                /*redissonLock.lock();
                redissonLock.lock();*/
            }
        }

        System.out.println(Thread.currentThread().getName() + " main ------ ends.");

        // 暂停几秒钟线程
        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
        redisson.shutdown();
    }
}
