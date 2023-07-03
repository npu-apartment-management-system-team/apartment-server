package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author wangminan
* @description 针对表【user(住宿职工表)】的数据库操作Mapper
* @createDate 2023-06-27 21:19:32
* @Entity edu.npu.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user " +
            "WHERE id % #{shardTotal} = #{shardIndex}")
    List<User> getListByShardIndex(Long shardIndex, Integer shardTotal);
}




