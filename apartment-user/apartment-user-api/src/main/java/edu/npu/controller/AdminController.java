package edu.npu.controller;

import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.RoleEnum;
import edu.npu.dto.AdminDto;
import edu.npu.dto.AdminPageQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.AdminService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [Admin表对应的控制器类]
 */

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R addAdmin(@RequestBody AdminDto adminDto) {
        return adminService.addAdmin(adminDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R deleteAdmin(@PathVariable("id") Long id) {
        return adminService.deleteAdmin(id);
    }

    @PutMapping("/{id}")
    public R updateAdmin(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable("id") Long id,
                         @RequestBody AdminDto adminDto) {
        if (userDetails.getAuthorities().contains(
                new SimpleGrantedAuthority(RoleEnum.USER.name()))) {
            return R.error(ResponseCodeEnum.FORBIDDEN,"权限不足");
        }
        return adminService.updateAdmin(id, adminDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R getAdminList(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                          AdminPageQueryDto adminPageQueryDto) {
        if (accountUserDetails.getRole() == RoleEnum.USER.getValue()){
            return R.error(ResponseCodeEnum.FORBIDDEN,"权限不足");
        }
        return adminService.getAdminList(adminPageQueryDto);
    }

    @GetMapping("/foreman")
    public R getForemanList(@AuthenticationPrincipal AccountUserDetails accountUserDetails) {
        if (accountUserDetails.getRole() == RoleEnum.USER.getValue()){
            return R.error(ResponseCodeEnum.FORBIDDEN,"权限不足");
        }
        return adminService.getForemanList();
    }
}
