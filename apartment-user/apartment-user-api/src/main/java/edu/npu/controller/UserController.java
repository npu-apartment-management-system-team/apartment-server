package edu.npu.controller;

import edu.npu.dto.UserPageQueryDto;
import edu.npu.dto.UserUpdateDto;
import edu.npu.service.UserService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */

@RestController("/user")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping
    public R getUsersInfo(UserPageQueryDto userPageQueryDto){
        return userService.getUsersInfo(userPageQueryDto);
    }

    @PutMapping("/{id}")
    public R updateUserInfo(@PathVariable("id") Long id, UserUpdateDto userUpdateDto){
        return userService.updateUserInfo(id,userUpdateDto);
    }
    @DeleteMapping("/{id}")
    public R deleteUserInfo(@PathVariable("id") Long id){
        return userService.deleteUser(id);
    }
}
