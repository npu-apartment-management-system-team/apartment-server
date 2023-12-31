package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author wangminan
* @description 针对表【department(部门表)】的数据库操作Mapper
* @createDate 2023-06-29 09:13:20
* @Entity edu.npu.entity.Department
*/
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    @Select("SELECT * FROM department " +
            "WHERE id % #{shardTotal} = #{shardIndex}")
    List<Department> getListByShardIndex(
            @Param("shardIndex") Long shardIndex,
            @Param("shardTotal") int shardTotal);
}




