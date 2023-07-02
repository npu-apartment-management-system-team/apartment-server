package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.entity.Admin;
import edu.npu.entity.User;
import edu.npu.service.AdminService;
import edu.npu.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [用户模块远程调用接口]
 */
@RestController
@RequestMapping("/remote")
public class RemoteController {

    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    @GetMapping("/admin")
    public Admin getAdminById(@RequestParam(value = "id") Long id) {
        return adminService.getById(id);
    }

    @GetMapping("/admin/list")
    public List<Admin> getAdminByDepartmentId(
            @RequestParam(value = "departmentId") Long departmentId) {
        return adminService.list(
            new LambdaQueryWrapper<Admin>()
                .eq(Admin::getDepartmentId, departmentId));
    }

    @GetMapping("/user/loginAccountId")
    public User getUserByLoginAccountId(
            @RequestParam(value = "loginAccountId") Long loginAccountId) {
        return userService.getOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getLoginAccountId, loginAccountId));
    }

    @GetMapping("/user")
    public User getUserById(@RequestParam(value = "id") Long id) {
        return userService.getById(id);
    }

    @GetMapping("/admin/loginAccountId")
    public Admin getAdminByLoginAccountId(
            @RequestParam(value = "id") Long id) {
        return adminService.getOne(
            new LambdaQueryWrapper<Admin>()
                .eq(Admin::getLoginAccountId, id));
    }

    @GetMapping("/user/department/list")
    public List<User> getUserListByDepartmentId(
            @RequestParam(value = "departmentId") Long departmentId) {
        return userService.list(
            new LambdaQueryWrapper<User>()
                .eq(User::getDepartmentId, departmentId));
    }

    @PutMapping("/user")
    public boolean updateUser(@RequestBody User user) {
        return userService.updateById(user);
    }

    @GetMapping("/user/bed")
    public List<User> getUserByBedId(@RequestParam(value = "bedId") Long bedId) {
        return userService.list(
            new LambdaQueryWrapper<User>()
                .eq(User::getBedId, bedId));
    }
}
