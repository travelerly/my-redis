package com.colin.redis.bloomfilter;

import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author colin
 * @create 2021-05-22 12:08
 */
public class RedisBloomFilterDemo {

    public static final int _1W = 10000;
    // 布隆过滤器里预计要插入多少数据
    public static int size = 100 * _1W;
    // 误判率,它越小误判的个数也就越少，但误判率越小，哈希函数越多，计算耗时越长，性能越低，0.03 是最优选择
    public static double fpp = 0.03;

    // redisson
    static RedissonClient redissonClient = null;
    // redis 版内置的布隆过滤器
    static RBloomFilter rBloomFilter = null;

    @Resource
    RedisTemplate redisTemplate;

    static {
        // redisson 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(0);
        // 构造 redisson
        redissonClient = Redisson.create(config);
        // 通过 redisson 构造 rBloomFilter
        rBloomFilter = redissonClient.getBloomFilter("phoneListBloomFilter",new StringCodec());
        rBloomFilter.tryInit(size,fpp);

        // 1.测试布隆过滤器有 + redis 有
        rBloomFilter.add("10086");
        redissonClient.getBucket("10086",new StringCodec()).set("chinamobile10086");

        // 2.测试布隆过滤器有 + redis 无
        //rBloomFilter.add("10087");

        //3 测试布隆过滤器无 + redis 无
    }

    private static String getPhoneListById(String IDNumber){

        String result = null;
        if (IDNumber == null) {
            return null;
        }

        // 1.先去布隆过滤器中查询
        if (rBloomFilter.contains(IDNumber)){
            // 2.布隆过滤器判断数据存在，再去 redis 中读取数据
            RBucket<String> rBucket = redissonClient.getBucket(IDNumber, new StringCodec());
            result = rBucket.get();
            if (result != null){
                // redis 中存在数据，直接返回结果
                return "i come from redis: "+result;
            }else {
                // redis 中不存在该数据，布隆过滤器发生了误判，再去数据库中查询数据
                result = getPhoneListByMySQL(IDNumber);
                if (result == null) {
                    // 数据库中也没有该条数据，直接返回空值
                    return null;
                }
                // 数据库中存在该条数据，重新将数据更新回 redis
                redissonClient.getBucket(IDNumber, new StringCodec()).set(result);
            }
            return "i come from mysql: "+result;
        }
        return result;
    }

    // 模拟从数据库中查询数据
    private static String getPhoneListByMySQL(String IDNumber) {
        return "chinamobile"+IDNumber;
    }

    public static void main(String[] args) {
        String phoneListById = getPhoneListById("10086");
        // String phoneListById = getPhoneListById("10087"); // 请测试执行2次
        // String phoneListById = getPhoneListById("10088");
        System.out.println("------查询出来的结果： "+phoneListById);

        //暂停几秒钟线程
        try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
        redissonClient.shutdown();
    }
}
