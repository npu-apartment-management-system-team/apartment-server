package edu.npu.controller;

import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.RoleEnum;
import edu.npu.dto.DepartmentDto;
import edu.npu.dto.DepartmentPageQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.DepartmentService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
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
