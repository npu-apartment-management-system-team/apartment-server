package edu.npu.controller;

import edu.npu.dto.UserListQueryDto;
import edu.npu.dto.UserUpdateDto;
import edu.npu.service.UserService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */

@RestController("/user")
public class UserController {
    @Resource
    private UserService userService;
    @GetMapping
    public R getUsersInfo(UserListQueryDto userListQueryDto){
        return userService.getUsersInfo(userListQueryDto);
    }
    @PutMapping("/{id}")
    public R updateUserInfo(@PathVariable("id") Long id, UserUpdateDto userUpdateDto){
        return userService.updateUserInfo(id,userUpdateDto);
    }
}
