package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Department;
import edu.npu.service.DepartmentService;
import edu.npu.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【department(部门表)】的数据库操作Service实现
* @createDate 2023-06-29 21:18:43
*/
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
    implements DepartmentService{

}




