package edu.npu.controller;

import edu.npu.entity.AccountUserDetails;
import edu.npu.service.MessageReceivingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [接收者控制器]
 */
@RestController
@Slf4j
@RequestMapping("/receiver")
public class ReceiverController {

    @Resource
    private MessageReceivingService messageReceivingService;

    @GetMapping("/inbox/detail")
    @PreAuthorize("hasAuthority('USER')")
    public R getMessageDetail(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                                  @RequestParam String id) {
        return messageReceivingService.getMessageDetail(accountUserDetails,id);
    }

    /**
     * @deprecated 因为ES表结构设计原因，无法实现接口功能，故弃用
     */
    @DeleteMapping("/inbox/{id}")
    @Deprecated(since="v0.0.6.0",forRemoval = true)
    @PreAuthorize("hasAuthority('USER')")
    public R DeleteMessage(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                           @PathVariable String id) {
        return messageReceivingService.deleteMessage(accountUserDetails,id);
    }


}
