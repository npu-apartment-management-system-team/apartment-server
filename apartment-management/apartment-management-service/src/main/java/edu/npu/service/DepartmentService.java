package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.DepartmentDto;
import edu.npu.dto.DepartmentPageQueryDto;
import edu.npu.entity.Department;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

/**
* @author wangminan
* @description 针对表【department(部门表)】的数据库操作Service
* @createDate 2023-06-29 09:13:20
*/
public interface DepartmentService extends IService<Department> {

    /**
     *新增外部单位信息
     * @param departmentDto 外部单位信息
     * @return R 成功或失败信息
     */
    @Transactional(rollbackFor = Exception.class)
    R addDepartment(DepartmentDto departmentDto);

    /**
     * 删除外部单位
     * @param id 外部单位id
     * @return R 成功或失败信息
     */
    @Transactional(rollbackFor = Exception.class)
    R deleteDepartment(Long id);

    /**
     * 修改外部单位信息
     * @param id 修改外部单位信息
     * @param departmentDto 外部单位信息
     * @return R 修改结果成功或失败信息
     */
    @Transactional(rollbackFor = Exception.class)
    R updateDepartment(Long id, DepartmentDto departmentDto);

    /**
     * 查询外部单位信息列表
     * @param departmentPageQueryDto 查询条件
     * @return R 外部单位列表
     */
    @Transactional(rollbackFor = Exception.class)
    R getDepartmentList(DepartmentPageQueryDto departmentPageQueryDto);

    /**
     * 查询外部单位信息简表
     * @return 简表
     */
    @Transactional(rollbackFor = Exception.class)
    R getDepartmentSimpleList();

    /**
     * 查看外部单位详细信息
     * @param id 外部单位ID
     * @return R
     */
    @Transactional(rollbackFor = Exception.class)
    R getDepartmentDetail(Long id);

}
