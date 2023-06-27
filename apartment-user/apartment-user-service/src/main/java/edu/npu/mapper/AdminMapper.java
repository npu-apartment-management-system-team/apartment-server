package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【admin(管理员)】的数据库操作Mapper
* @createDate 2023-06-27 21:19:31
* @Entity edu.npu.entity.Admin
*/
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

}




