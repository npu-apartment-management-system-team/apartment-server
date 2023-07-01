package edu.npu.controller;

import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.RoleEnum;
import edu.npu.dto.DepartmentDto;
import edu.npu.dto.DepartmentPageQueryDto;
import edu.npu.service.DepartmentService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [单位控制器类]
 */
@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R addDepartment(@RequestBody DepartmentDto departmentDto) {
        return departmentService.addDepartment(departmentDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R deleteDepartment(@PathVariable("id") Long id) {
        return departmentService.deleteDepartment(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R updateDepartment(@PathVariable("id") Long id,
                              @RequestBody DepartmentDto departmentDto) {
        return departmentService.updateDepartment(id, departmentDto);

    }

    @GetMapping
    public R getDepartmentList(DepartmentPageQueryDto departmentPageQueryDto) {
        if (departmentPageQueryDto.latitude() > 90 ||
                departmentPageQueryDto.latitude() < -90 ||
                departmentPageQueryDto.longitude() > 180 ||
                departmentPageQueryDto.longitude() < -180) {
            return R.error(
                    ResponseCodeEnum.PRE_CHECK_FAILED,
                    "经纬度范围不正确");
        }
        return departmentService.getDepartmentList(departmentPageQueryDto);

    }

    @GetMapping("/list")
    public R getDepartmentSimpleList(@AuthenticationPrincipal UserDetails userDetails){
        if (userDetails.getAuthorities().contains(
                new SimpleGrantedAuthority(RoleEnum.USER.name()))){
            return R.error(ResponseCodeEnum.FORBIDDEN, "权限不足");
        }
        return departmentService.getDepartmentSimpleList();
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R getDepartmentDetail(Long id) {
        return departmentService.getDepartmentDetail(id);
    }


}
