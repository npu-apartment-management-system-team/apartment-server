package edu.npu.controller;

import edu.npu.dto.SendMessageDto;
import edu.npu.service.MessageDetailService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [发送者控制器类]
 */
@RestController
@Slf4j
@RequestMapping("/sender")
public class SenderController {

    @Resource
    private MessageDetailService messageDetailService;

    @PostMapping("/send")
    public R sendMessage(@RequestBody SendMessageDto sendMessageDto) {
        return messageDetailService.sendMessage(sendMessageDto);
    }

    @GetMapping("/outbox/detail")
    public R getMessageDetail(@RequestParam String id) {
        return messageDetailService.getMessageDetail(id);
    }

    @PutMapping("/outbox/{id}")
    public R withdrawMessage(@PathVariable String id) {
        return messageDetailService.withdrawMessage(id);
    }

    @DeleteMapping("/outbox/{id}")
    public R deleteMessage(@PathVariable String id) {
        return messageDetailService.deleteMessage(id);
    }
}
