package edu.npu.controller;

import edu.npu.common.UserPayStatusEnum;
import edu.npu.common.UserPayTypeEnum;
import edu.npu.entity.PaymentUser;
import edu.npu.service.PaymentUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static edu.npu.common.PaymentConstants.DEPOSIT_FEE;

/**
 * @author : [wangminan]
 * @description : [远程调用控制器类]
 */
@RestController
@RequestMapping("/remote")
public class RemoteController {

    @Resource
    private PaymentUserService paymentUserService;

    @PostMapping("/deposit/{userId}")
    public boolean addDepositCharge(@PathVariable(value = "userId") Long userId) {
        PaymentUser paymentUser = PaymentUser.builder()
                .userId(userId)
                .price(DEPOSIT_FEE)
                .type(UserPayTypeEnum.DEPOSIT.getValue())
                .status(UserPayStatusEnum.UNPAID.getValue())
                .createTime(new Date(System.currentTimeMillis()))
                .updateTime(new Date(System.currentTimeMillis()))
                .build();
        return paymentUserService.save(paymentUser);
    }
}
