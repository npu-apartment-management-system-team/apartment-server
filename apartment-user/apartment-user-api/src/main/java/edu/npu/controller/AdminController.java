package edu.npu.controller;

import edu.npu.dto.AdminDto;
import edu.npu.dto.AdminPageQueryDto;
import edu.npu.service.AdminService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping
    public R addAdmin(AdminDto adminDto) {
        return adminService.addAdmin(adminDto);
    }

    @DeleteMapping("/{id}")
    public R deleteAdmin(@PathVariable("id") Long id) {
        return adminService.deleteAdmin(id);
    }

    @PutMapping("/{id}")
    public R updateAdmin(@PathVariable("id") Long id, AdminDto adminDto) {
        return adminService.updateAdmin(id, adminDto);
    }

    @GetMapping
    public R getAdminList(AdminPageQueryDto adminPageQueryDto) {
        return adminService.getAdminList(adminPageQueryDto);
    }

    @GetMapping("/foreman")
    public R getForemanList() {
        return adminService.getForemanList();
    }
}
