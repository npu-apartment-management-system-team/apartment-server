package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.PaymentDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author wangminan
* @description 针对表【payment_department(代扣外部单位缴费表)】的数据库操作Mapper
* @createDate 2023-07-02 16:45:55
* @Entity edu.npu.entity.PaymentDepartment
*/
@Mapper
public interface PaymentDepartmentMapper extends BaseMapper<PaymentDepartment> {
}




