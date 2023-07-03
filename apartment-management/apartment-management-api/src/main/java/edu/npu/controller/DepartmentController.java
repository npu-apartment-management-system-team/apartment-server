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
        if (departmentDto.positionLongitude() > 180 ||
                departmentDto.positionLongitude() < -180 ||
                departmentDto.positionLatitude() > 90 ||
                departmentDto.positionLatitude() < -90) {
            return R.error(
                    ResponseCodeEnum.PRE_CHECK_FAILED,
                    "经纬度范围不正确");
        }
        return departmentService.updateDepartment(id, departmentDto);

    }

    @GetMapping
    public R getDepartmentList(DepartmentPageQueryDto departmentPageQueryDto) {
        if (preCheckPosition(
                departmentPageQueryDto.latitude(),
                departmentPageQueryDto.longitude()))
            return R.error(
                    ResponseCodeEnum.PRE_CHECK_FAILED,
                    "经纬度范围不正确");
        return departmentService.getDepartmentList(departmentPageQueryDto);

    }

    private static boolean preCheckPosition(Double latitude,
                                    Double longitude) {
        if (latitude != null && longitude != null) {
            return latitude > 90 ||
                    latitude < -90 ||
                    longitude > 180 ||
                    longitude < -180;
        }
        return false;
    }

    @GetMapping("/list")
    public R getDepartmentSimpleList(@AuthenticationPrincipal UserDetails userDetails){
        // 该接口允许用户访问
        return departmentService.getDepartmentSimpleList();
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public R getDepartmentDetail(Long id) {
        return departmentService.getDepartmentDetail(id);
    }


}
