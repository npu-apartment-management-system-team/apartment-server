package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Application;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【application(申请表)】的数据库操作Mapper
* @createDate 2023-06-29 21:18:43
* @Entity edu.npu.entity.Application
*/
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

}




