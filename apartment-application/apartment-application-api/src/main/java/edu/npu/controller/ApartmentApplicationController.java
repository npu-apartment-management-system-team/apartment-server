package edu.npu.controller;

import edu.npu.dto.BasicPageQueryDto;
import edu.npu.dto.BasicReviewDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.ApartmentApplicationService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : 公寓侧管理控制器类
 */
@RestController
@RequestMapping("/apply/status/apartment")
public class ApartmentApplicationController {

    @Resource
    private ApartmentApplicationService applicationService;

    @GetMapping
    @PreAuthorize("hasAuthority('CENTER_DORM_MANAGER')")
    public R getApplicationList(
            @AuthenticationPrincipal AccountUserDetails accountUserDetails,
            @Validated BasicPageQueryDto basicPageQueryDto
    ) {
        return applicationService.getApplicationList(
                accountUserDetails, basicPageQueryDto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CENTER_DORM_MANAGER')")
    public R statusChangeConfirm(
            @Validated @RequestBody BasicReviewDto reviewDto
    ) {
        return applicationService.statusChangeConfirm(reviewDto);
    }
}
