package edu.npu.controller;

import edu.npu.dto.BasicPageQueryDto;
import edu.npu.dto.BasicReviewDto;
import edu.npu.service.ApartmentApplicationService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @Validated BasicPageQueryDto basicPageQueryDto
    ) {
        return applicationService.getApplicationList(basicPageQueryDto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CENTER_DORM_MANAGER')")
    public R statusChangeConfirm(
            @Validated BasicReviewDto reviewDto
    ) {
        return applicationService.statusChangeConfirm(reviewDto);
    }
}
