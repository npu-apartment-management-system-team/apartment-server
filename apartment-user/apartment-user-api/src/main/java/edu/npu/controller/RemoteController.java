package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.entity.Admin;
import edu.npu.entity.User;
import edu.npu.mapper.UserMapper;
import edu.npu.service.AdminService;
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
    private UserMapper userMapper;

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
        return userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getLoginAccountId, loginAccountId));
    }

    @GetMapping("/user")
    public User getUserById(@RequestParam(value = "id") Long id) {
        return userMapper.selectById(id);
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
        return userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getDepartmentId, departmentId));
    }

    @PutMapping("/user")
    public boolean updateUser(@RequestBody User user) {
        return userMapper.updateById(user) == 1;
    }

    @GetMapping("/user/bed")
    public List<User> getUsersByBedId(@RequestParam(value = "bedId") Long bedId) {
        return userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getBedId, bedId));
    }

    @GetMapping("/room/list/shard")
    public List<User> getListByShardIndex(
            @RequestParam(value = "shardIndex") Long shardIndex,
            @RequestParam(value = "shardTotal") Integer shardTotal) {
        return userMapper.getListByShardIndex(shardIndex, shardTotal);
    }
}
