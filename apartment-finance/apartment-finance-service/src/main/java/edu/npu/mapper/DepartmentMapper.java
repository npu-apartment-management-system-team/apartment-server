package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Yu
 * @Date: 2023/7/3
 * @Description: 针对表【department】的数据库操作Mapper
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
