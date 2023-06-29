package edu.npu.mapper;

import edu.npu.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【department(部门表)】的数据库操作Mapper
* @createDate 2023-06-29 09:13:20
* @Entity edu.npu.entity.Department
*/
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

}




