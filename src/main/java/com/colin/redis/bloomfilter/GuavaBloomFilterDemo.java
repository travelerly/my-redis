package com.colin.redis.bloomfilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.util.ArrayList;

/**
 * @author colin
 * @create 2021-05-22 11:00
 */
public class GuavaBloomFilterDemo {

    public static final int _1W = 10000;
    //布隆过滤器里预计要插入多少数据
    public static int size = 100 * _1W;
    //误判率,它越小误判的个数也就越少，但误判率越小，哈希函数越多，计算耗时越长，性能越低，0.03是最优选择
    public static double fpp = 0.03;

    /**
     * Guava布隆过滤器入门
     */
    public static void bloomFilter_1(){
        // 创建布隆过滤器对象
        BloomFilter<Integer> filter = BloomFilter.create(Funnels.integerFunnel(), 100);

        // 判断指定元素是否存在
        System.out.println(filter.mightContain(1));
        System.out.println(filter.mightContain(2));

        // 将元素添加进布隆过滤器中
        filter.put(1);
        filter.put(2);

        // 再次判断指定元素是否存在
        System.out.println(filter.mightContain(1));
        System.out.println(filter.mightContain(2));
        System.out.println(filter.mightContain(3));
    }

    public static void bloomFilter_2(){
        // 构建布隆过滤器
        BloomFilter<Integer> filter = BloomFilter.create(Funnels.integerFunnel(), size, fpp);
        // 先向布隆过滤器中添加100w条数据
        for (int i = 0; i < size; i++) {
            filter.put(i);
        }

        // 模拟判断：这100万的数据是否存在布隆过滤器中
        ArrayList<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            if (filter.mightContain(i)){
                list.add(i);
                continue;
            }
        }
        System.out.println("布隆过滤器中存在的数量是："+list.size());
        System.out.println("=============================");

        //模拟判断：取10w不在布隆过滤器中的值，计算误判率
        ArrayList<Object> error_list = new ArrayList<>(size);
        for (int i = size+1; i < size + 100000; i++) {
            if (filter.mightContain(i)){
                System.out.println("当前值："+i+"，布隆过滤器存在误判");
                error_list.add(i);
            }
        }
        System.out.println("布隆过滤器误判的数量是："+error_list.size());
    }

    public static void main(String[] args) {
        //bloomFilter_1();
        bloomFilter_2();
    }
}
