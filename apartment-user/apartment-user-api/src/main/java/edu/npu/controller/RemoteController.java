package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.entity.Admin;
import edu.npu.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping
    public List<Admin> getAdminByDepartmentId(
            @RequestParam(value = "departmentId") Long departmentId) {
        return adminService.list(
            new LambdaQueryWrapper<Admin>()
                .eq(Admin::getDepartmentId, departmentId));
    }
}
