package com.colin.redis.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.colin.redis.entities.User;
import com.colin.redis.entities.UserDTO;
import com.colin.redis.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Random;

/**
 * @author colin
 * @create 2021-05-13 23:06
 */
@RestController("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation("数据库新增五条记录")
    @PostMapping("/addUser")
    public void addUser(){
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setUsername("colin_"+i);
            user.setPassword(IdUtil.simpleUUID().substring(0,6));
            user.setSex((byte) new Random().nextInt(2));
            userService.addUser(user);
        }
    }

    @ApiOperation("删除指定用户数据")
    @PostMapping("/deleteUser/{id}")
    public void deleteUser(@PathVariable Integer id){
        userService.deleteUser(id);
    }

    @ApiOperation("更新用户数据")
    @PostMapping("/updateUser")
    public void updateUser(@RequestBody UserDTO userDTO){
        User user = new User();
        BeanUtil.copyProperties(userDTO,user);
        userService.updateUser(user);
    }

    @ApiOperation("查询指定用户数据")
    @GetMapping("/findUserById/{id}")
    public User findUserById(@PathVariable Integer id){
        return userService.findUserById(id);
    }
}
