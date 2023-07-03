package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.PaymentDepartment;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【payment_department(代扣外部单位缴费表)】的数据库操作Service
* @createDate 2023-07-02 16:45:55
*/
public interface PaymentDepartmentService extends IService<PaymentDepartment> {

    R getVariationList(UserPayListQueryDto userPayListQueryDto);

    R downloadVariationList(DownloadQueryDto downloadQueryDto);



}
