package edu.npu.service;

import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.PaymentUser;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【payment_user(自收与代扣自付部分缴费表)】的数据库操作Service
* @createDate 2023-07-02 16:45:55
*/
public interface PaymentUserService extends IService<PaymentUser> {

    R getPayList(AccountUserDetails accountUserDetails, UserPayListQueryDto userPayListQueryDto);

    R startPay(AccountUserDetails accountUserDetails, Long id);
}
