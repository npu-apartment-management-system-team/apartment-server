package edu.npu.controller;

import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.PaymentUserService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : [wangminan]
 * @description : [职工缴费控制器类]
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private PaymentUserService paymentUserService;

    @GetMapping("/pay/list")
    public R getPayList(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                        @Validated UserPayListQueryDto userPayListQueryDto) {
        return paymentUserService.getPayList(accountUserDetails, userPayListQueryDto);
    }

    @PostMapping("/pay/{id}")
    public R startPay(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                      @PathVariable("id") Long id) {
        return paymentUserService.startPay(accountUserDetails, id);
    }
}
