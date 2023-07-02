package edu.npu.controller;

import edu.npu.service.MessageReceivingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public R getMessageDetail(@RequestParam String id) {
        return messageReceivingService.getMessageDetail(id);
    }

    @DeleteMapping("/inbox/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public R DeleteMessage(@PathVariable String id) {
        return messageReceivingService.deleteMessage(id);
    }


}
