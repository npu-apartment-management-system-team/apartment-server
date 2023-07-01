package edu.npu.controller;

import edu.npu.dto.BasicPageQueryDto;
import edu.npu.dto.BasicReviewDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.DepartmentApplicationService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [外部单位申请处理Controller]
 */
@RestController
@RequestMapping("/apply/status/department")
public class DepartmentApplicationController {

    @Resource
    private DepartmentApplicationService departmentService;

    @PreAuthorize("hasAuthority('DEPARTMENT_CHECK_IN_CLERK')")
    @GetMapping
    public R getApplicationListForDepartment(
            @AuthenticationPrincipal AccountUserDetails accountUserDetails,
            @Validated BasicPageQueryDto basicPageQueryDto) {
        return departmentService.getApplicationListForDepartment(
                accountUserDetails,basicPageQueryDto);
    }

    @PreAuthorize("hasAuthority('DEPARTMENT_CHECK_IN_CLERK')")
    @PostMapping
    public R reviewApplicationForDepartment(
            @AuthenticationPrincipal AccountUserDetails accountUserDetails,
            @Validated @RequestBody BasicReviewDto basicReviewDto) {
        return departmentService.reviewApplicationForDepartment(
                accountUserDetails, basicReviewDto);
    }
}
