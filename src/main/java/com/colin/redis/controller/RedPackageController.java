package com.colin.redis.controller;

import cn.hutool.core.util.IdUtil;
import com.google.common.primitives.Ints;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author colin
 * @create 2021-12-23 12:31
 *
 * 模拟微信抢红包
 * 各种节假日，发红包 + 抢红包，不说了，100% 高并发业务要求，不能用 mysql 来做
 * 一个总的大红包，会有可能拆分成多个小红包，总金额 = 分金额1 + 分金额2 + 分金额3......分金额N
 * 每个人只能抢一次，你需要有记录，比如 100 块钱，被拆分成 10 个红包发出去，总计有 10 个红包，抢一个少一个，总数显示(10/6)直到完，需要记录那些人抢到了红包，重复抢作弊不可以。
 * 有可能还需要你计时，完整抢完，从发出到全部 over，耗时多少？
 * 红包过期，或者群主人品差，没人抢红包，原封不动退回。
 * 红包过期，剩余金额可能需要回退到发红包主账户下。
 */
public class RedPackageController {

    public static final String RED_PACKAGE_KEY = "redpackage:";
    public static final String RED_PACKAGE_CONSUME_KEY = "redpackage:consume:";

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 拆分+发送红包
     * http://localhost:5555/send?totalMoney=100&redPackageNumber=5
     * @param totalMoney
     * @param redPackageNumber
     * @return
     */
    @RequestMapping("/send")
    public String sendRedPackage(int totalMoney,int redPackageNumber) {
        //1 拆红包，总金额拆分成多少个红包，每个小红包里面包多少钱
        Integer[] splitRedPackages = splitRedPackage(totalMoney, redPackageNumber);
        //2 红包的全局ID
        String key = RED_PACKAGE_KEY+ IdUtil.simpleUUID();
        //3 采用list存储红包并设置过期时间
        redisTemplate.opsForList().leftPushAll(key,splitRedPackages);
        redisTemplate.expire(key,1, TimeUnit.DAYS);
        return key+"\t"+"\t"+ Ints.asList(Arrays.stream(splitRedPackages).mapToInt(Integer::valueOf).toArray());
    }

    /**
     * http://localhost:5555/rob?redPackageKey=上一步的红包UUID&userId=1
     * @param redPackageKey
     * @param userId
     * @return
     */
    @RequestMapping("/rob")
    public String rodRedPackage(String redPackageKey,String userId) {
        //1 验证某个用户是否抢过红包
        Object redPackage = redisTemplate.opsForHash().get(RED_PACKAGE_CONSUME_KEY + redPackageKey, userId);
        //2 没有抢过就开抢，否则返回-2表示抢过
        if (redPackage == null) {
            // 2.1 从list里面出队一个红包，抢到了一个
            Object partRedPackage = redisTemplate.opsForList().leftPop(RED_PACKAGE_KEY + redPackageKey);
            if (partRedPackage != null) {
                //2.2 抢到手后，记录进去hash表示谁抢到了多少钱的某一个红包
                redisTemplate.opsForHash().put(RED_PACKAGE_CONSUME_KEY + redPackageKey,userId,partRedPackage);
                System.out.println("用户: "+userId+"\t 抢到多少钱红包: "+partRedPackage);
                //TODO 后续异步进mysql或者RabbitMQ进一步处理
                return String.valueOf(partRedPackage);
            }
            //抢完
            return "errorCode:-1,红包抢完了";
        }
        //3 某个用户抢过了，不可以作弊重新抢
        return "errorCode:-2,   message: "+"\t"+userId+" 用户你已经抢过红包了";
    }

    /**
     * 红包拆分算法
     * 1 拆完红包总金额+每个小红包金额别太离谱
     *
     * 二倍均值法
     * 剩余红包金额为 M，剩余人数为 N，那么有如下公式：
     * 每次抢到的金额 = 随机区间（0， (剩余红包金额M ÷ 剩余人数N ) X 2）
     * 这个公式，保证了每次随机金额的平均值是相等的，不会因为抢红包的先后顺序而造成不公平。
     * 举个栗子：
     * 假设有 10 个人，红包总额 100 元。
     * 第 1 次：
     * 100 ÷ 10 X 2 = 20, 所以第一个人的随机范围是（0，20)，平均可以抢到 10 元。假设第一个人随机到 10 元，那么剩余金额是 100 - 10 = 90 元。
     * 第 2 次：
     * 90 ÷ 9 X2 = 20, 所以第二个人的随机范围同样是（0，20 )，平均可以抢到 10 元。假设第二个人随机到 10 元，那么剩余金额是 90 - 10 = 80 元。
     * 第 3 次：
     * 80 ÷ 8 X2 = 20, 所以第三个人的随机范围同样是（0，20 )，平均可以抢到 10 元。以此类推，每一次随机范围的均值是相等的。
     *
     * @param totalMoney
     * @param redPackageNumber
     * @return
     */
    private Integer[] splitRedPackage(int totalMoney, int redPackageNumber) {
        int useMoney = 0;
        Integer[] redPackageNumbers = new Integer[redPackageNumber];
        Random random = new Random();

        for (int i = 0; i < redPackageNumber; i++) {
            if(i == redPackageNumber - 1) {
                redPackageNumbers[i] = totalMoney - useMoney;
            }else{
                int avgMoney = (totalMoney - useMoney) * 2 / (redPackageNumber - i);
                redPackageNumbers[i] = 1 + random.nextInt(avgMoney - 1);
            }
            useMoney = useMoney + redPackageNumbers[i];
        }
        return redPackageNumbers;
    }
}
