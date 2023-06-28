package edu.npu.controller;

import edu.npu.dto.BindAlipayCallbackDto;
import edu.npu.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AlipayBindController {
    @Resource
    private UserService userService;

    @GetMapping("/bind/alipay/callback")
    public String bindAlipayCallback(BindAlipayCallbackDto bindAlipayCallbackDto){
        return userService.bindAlipayToUser(bindAlipayCallbackDto);
    }
}
