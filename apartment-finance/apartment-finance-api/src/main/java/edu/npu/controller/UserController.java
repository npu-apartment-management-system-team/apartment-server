package edu.npu.controller;

import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.PaymentUserService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author : [wangminan]
 * @description : [职工缴费控制器类]
 */
@RestController
@RequestMapping("/user")
@Slf4j
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

    /**
     * 支付通知回调接口，该接口由支付宝开放平台调用，与前端无关
     * @param notifyParams 支付宝开放平台回调参数
     * @return 对支付宝开放平台响应校验后的结果
     */
    @PostMapping("/pay/notify")
    public String tradeNotify(@RequestParam Map<String, String> notifyParams){
        log.info("支付通知回调");
        log.info("通知参数 ====> {}", notifyParams);

        return paymentUserService.checkSignAndConfirm(notifyParams);
    }

    /**
     * 用户完成支付后更新订单状态
     * @param accountUserDetails 用户信息
     * @param paymentId 订单号
     * @return R
     */
    @PutMapping("/pay/{paymentId}")
    public R updatePay(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                       @PathVariable("paymentId") Long paymentId) {
        return paymentUserService.updatePay(accountUserDetails, paymentId);
    }
}
