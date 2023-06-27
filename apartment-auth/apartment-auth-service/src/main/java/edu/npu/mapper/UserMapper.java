package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【user(住宿职工表)】的数据库操作Mapper
* @createDate 2023-06-26 21:19:29
* @Entity edu.npu.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




