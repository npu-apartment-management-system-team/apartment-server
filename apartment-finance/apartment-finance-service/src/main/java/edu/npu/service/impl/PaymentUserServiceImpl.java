package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.PaymentUser;
import edu.npu.service.PaymentUserService;
import edu.npu.mapper.PaymentUserMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【payment_user(自收与代扣自付部分缴费表)】的数据库操作Service实现
* @createDate 2023-07-02 16:45:55
*/
@Service
public class PaymentUserServiceImpl extends ServiceImpl<PaymentUserMapper, PaymentUser>
    implements PaymentUserService{

}




