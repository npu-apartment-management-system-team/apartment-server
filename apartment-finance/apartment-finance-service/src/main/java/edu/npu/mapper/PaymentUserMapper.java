package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.PaymentUser;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【payment_user(自收与代扣自付部分缴费表)】的数据库操作Mapper
* @createDate 2023-07-02 16:45:55
* @Entity edu.npu.entity.PaymentUser
*/
@Mapper
public interface PaymentUserMapper extends BaseMapper<PaymentUser> {

}




