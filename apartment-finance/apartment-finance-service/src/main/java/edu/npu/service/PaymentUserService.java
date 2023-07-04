package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.QueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.PaymentUser;
import edu.npu.vo.R;

import java.util.Map;

/**
* @author wangminan
* @description 针对表【payment_user(自收与代扣自付部分缴费表)】的数据库操作Service
* @createDate 2023-07-02 16:45:55
*/
public interface PaymentUserService extends IService<PaymentUser> {

    R getPayList(AccountUserDetails accountUserDetails, QueryDto queryDto);

    R startPay(AccountUserDetails accountUserDetails, Long id);

    String checkSignAndConfirm(Map<String, String> notifyParams);

    boolean refundDepositCharge(Long userId);

    boolean tradeQuery(String orderId);

    R updatePay(AccountUserDetails accountUserDetails, Long paymentId);
}
