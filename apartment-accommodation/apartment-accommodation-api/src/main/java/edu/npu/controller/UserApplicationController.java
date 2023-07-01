package edu.npu.controller;

import edu.npu.dto.UserApplicationDto;
import edu.npu.dto.UserStatusPageQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.UserApplicationService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [住宿职工申请控制器类]
 */
@RestController
public class UserApplicationController {

    @Resource
    private UserApplicationService userApplicationService;

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/apply/status")
    public R getApplicationStatus(
            @AuthenticationPrincipal AccountUserDetails accountUserDetails,
            @Validated UserStatusPageQueryDto pageQueryDto) {
        return userApplicationService.getApplicationStatus(
                accountUserDetails, pageQueryDto);
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('USER')")
    public R handleUserApplication(
            @AuthenticationPrincipal AccountUserDetails accountUserDetails,
            @Validated @RequestBody UserApplicationDto userApplicationDto) {
        return userApplicationService.handleSaveUserApplication(
                accountUserDetails, userApplicationDto);
    }

    @PutMapping("/apply/{id}")
    public R handleWithdrawApplication(
            @AuthenticationPrincipal AccountUserDetails accountUserDetails,
            @PathVariable(value = "id") Integer id) {
        return userApplicationService.handleWithdrawApplication(
                accountUserDetails, id);
    }
}
