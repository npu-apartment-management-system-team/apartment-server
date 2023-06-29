package edu.npu.service;

import edu.npu.dto.DepartmentDto;
import edu.npu.dto.DepartmentPageQueryDto;
import edu.npu.entity.Department;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

/**
* @author wangminan
* @description 针对表【department(部门表)】的数据库操作Service
* @createDate 2023-06-29 09:13:20
*/
public interface DepartmentService extends IService<Department> {

    @Transactional(rollbackFor = Exception.class)
    R addDepartment(DepartmentDto departmentDto);

    @Transactional(rollbackFor = Exception.class)
    R deleteDepartment(Long id);

    @Transactional(rollbackFor = Exception.class)
    R updateDepartment(Long id, DepartmentDto departmentDto);

    @Transactional(rollbackFor = Exception.class)
    R getDepartmentList(DepartmentPageQueryDto departmentPageQueryDto);

    @Transactional(rollbackFor = Exception.class)
    R getDepartmentSimpleList();

    @Transactional(rollbackFor = Exception.class)
    R getDepartmentDetail(Long id);

}
