package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.PaymentDepartment;
import edu.npu.service.PaymentDepartmentService;
import edu.npu.mapper.PaymentDepartmentMapper;
import edu.npu.vo.R;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【payment_department(代扣外部单位缴费表)】的数据库操作Service实现
* @createDate 2023-07-02 16:45:55
*/
@Service
public class PaymentDepartmentServiceImpl extends ServiceImpl<PaymentDepartmentMapper, PaymentDepartment>
    implements PaymentDepartmentService{

    @Override
    public R getVariationList(UserPayListQueryDto userPayListQueryDto) {
        return null;
    }

    @Override
    public R downloadVariationList(DownloadQueryDto downloadQueryDto) {
        return null;
    }
}




