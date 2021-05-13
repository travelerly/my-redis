package com.colin.redis.service;

import com.colin.redis.entities.User;
import com.colin.redis.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author colin
 * @create 2021-05-13 23:06
 */
@Service
@Slf4j
public class UserService {

    public static final String CACHE_KEY_USER = "user:";

    @Resource
    public UserMapper userMapper;
    @Resource
    private RedisTemplate redisTemplate;

    public void addUser(User user) {
        // 先将数据插入 mysql
        int i = userMapper.insertSelective(user);

        if (i>0){
            // 再查询 mysql，将刚插入的数据捞出，再存入 redis 中，完成数据库与缓存数据的一致性
            user = userMapper.selectByPrimaryKey(user.getId());
            String key = CACHE_KEY_USER + user.getId();
            redisTemplate.opsForValue().set(key,user);
        }
    }

    public void deleteUser(Integer id) {
        // 删除数据库中数据
        int i = userMapper.deleteByPrimaryKey(id);
        if (i>0){
            // 删除缓存中的数据
            String key = CACHE_KEY_USER + id;
            redisTemplate.delete(key);
        }
    }

    public void updateUser(User user) {
        // 先更新 mysql 中数据
        int i = userMapper.updateByPrimaryKey(user);
        if (i>0){
            // 再此查询 mysql，将刚更新的数据捞出，再存入 redis 中，完成数据库与缓存数据的一致性
            user = userMapper.selectByPrimaryKey(user.getId());
            String key = CACHE_KEY_USER + user.getId();
            redisTemplate.opsForValue().set(key,user);
        }
    }

    public User findUserById(Integer id) {
        // 先从 redis 中查询数据
        String key = CACHE_KEY_USER + id;
        User user = (User) redisTemplate.opsForValue().get(key);
        if (user == null){
            // redis 中没有数据，则需查询 mysql，利用双端检索机制，减少缓存击穿的影响
            synchronized (UserService.class){
                // 双端检索，二次判断
                user = (User) redisTemplate.opsForValue().get(key);
                if (user == null){
                    // 从 mysql 中查询数据
                    user = userMapper.selectByPrimaryKey(id);
                    if (user == null){
                        // mysql 中没有数据，则说明此数据不存在，但由于采用了双端检索机制，可以减少缓存击穿的影响
                        return null;
                    }else {
                        // 数据缓存到 redis 中
                        key = CACHE_KEY_USER + user.getId();
                        redisTemplate.opsForValue().setIfAbsent(key,user,7, TimeUnit.DAYS);
                    }
                }
            }
        }
        return user;
    }
}
