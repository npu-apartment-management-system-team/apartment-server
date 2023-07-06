package edu.npu.controller;

import edu.npu.dto.AllocationDto;
import edu.npu.dto.BasicPageQueryDto;
import edu.npu.dto.BasicReviewDto;
import edu.npu.service.CenterApplicationService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [房建段申请处理Controller]
 */
@RestController
@RequestMapping("/apply/status/center")
public class CenterApplicationController {

    @Resource
    private CenterApplicationService centerApplicationService;

    @GetMapping("/checkin")
    @PreAuthorize("hasAuthority('CENTER_CHECK_IN_CLERK')")
    public R getApplicationListForCenter(
            @Validated BasicPageQueryDto basicPageQueryDto) {
        return centerApplicationService.getApplicationListForCenter(
                basicPageQueryDto
        );
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasAuthority('CENTER_CHECK_IN_CLERK')")
    public R reviewApplicationForCenter(
            @RequestBody @Validated BasicReviewDto basicReviewDto) {
        return centerApplicationService.reviewApplicationForCenter(
                basicReviewDto
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CENTER_DORM_ALLOCATION_CLERK')")
    public R getWaitingAllocateList(
            @Validated BasicPageQueryDto basicPageQueryDto
    ) {
        return centerApplicationService.getWaitingAllocateList(
                basicPageQueryDto
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CENTER_DORM_ALLOCATION_CLERK')")
    public R handleAllocateBed(
            @RequestBody @Validated AllocationDto allocationDto
    ) {
        return centerApplicationService.handleAllocateBed(allocationDto);
    }
}
