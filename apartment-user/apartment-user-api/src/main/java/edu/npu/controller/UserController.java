package edu.npu.controller;

import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.RoleEnum;
import edu.npu.dto.UserPageQueryDto;
import edu.npu.dto.UserUpdateDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.LoginAccount;
import edu.npu.service.UserService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [user表控制器类 住宿职工管理]
 */

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping
    public R getUsersInfo(@AuthenticationPrincipal UserDetails userDetails,
                          UserPageQueryDto userPageQueryDto){
        if (userDetails.getAuthorities().contains(
                new SimpleGrantedAuthority(RoleEnum.USER.name()))){
            return R.error(ResponseCodeEnum.FORBIDDEN, "权限不足");
        }
        return userService.getUsersInfo(userPageQueryDto);
    }

    @GetMapping("/detail")
    public R getUserInfo(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                         @RequestParam(value = "id") Long id){
        if (accountUserDetails.getRole() == RoleEnum.USER.getValue() &&
                !accountUserDetails.getId().equals(id)
        ) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "您仅可访问自己的信息");
        }
        return userService.getUserInfo(id);
    }

    @PutMapping("/{id}")
    public R updateUserInfo(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                            @PathVariable("id") Long id, UserUpdateDto userUpdateDto){
        if (accountUserDetails.getRole() == RoleEnum.USER.getValue() &&
                !accountUserDetails.getId().equals(id)
        ) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "您仅可修改自己的信息");
        }
        return userService.updateUserInfo(id,userUpdateDto);
    }
    @DeleteMapping("/{id}")
    public R deleteUserInfo(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                            @PathVariable("id") Long id){
        if (accountUserDetails.getRole() == RoleEnum.USER.getValue() &&
                !accountUserDetails.getId().equals(id)
        ) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "您仅可删除自己的信息");
        }
        return userService.deleteUser(id);
    }
}
