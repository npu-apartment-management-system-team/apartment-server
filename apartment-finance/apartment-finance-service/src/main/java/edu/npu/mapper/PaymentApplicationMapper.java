package edu.npu.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Application;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Yu
 * @Date: 2023/7/3
 * @Description: 针对表【application】的数据库操作Mapper
 */
@Mapper
public interface PaymentApplicationMapper extends BaseMapper<Application> {
}
