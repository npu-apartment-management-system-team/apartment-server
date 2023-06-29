package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.DepartmentDto;
import edu.npu.dto.DepartmentPageQueryDto;
import edu.npu.entity.Admin;
import edu.npu.entity.Department;
import edu.npu.exception.ApartmentException;
import edu.npu.mapper.AdminMapper;
import edu.npu.service.DepartmentService;
import edu.npu.mapper.DepartmentMapper;
import edu.npu.util.RedisClient;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static edu.npu.common.RedisConstants.*;

/**
* @author wangminan
* @description 针对表【department(部门表)】的数据库操作Service实现
* @createDate 2023-06-29 09:13:20
*/
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
    implements DepartmentService {

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private RedisClient redisClient;


    /**
     *新增外部单位信息
     * @param departmentDto
     * @return R 成功或失败信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addDepartment(DepartmentDto departmentDto) {

        Department department = new Department();
        department.setName(departmentDto.name());
        department.setPosition(departmentDto.position());
        department.setPayType(departmentDto.payType());
        department.setIsInterior(departmentDto.isInterior());
        department.setPositionLongitude(Double.parseDouble(departmentDto.positionLongitude()));
        department.setPositionLatitude(Double.parseDouble(departmentDto.positionLatitude()));

        /*
        新增department
         */
        boolean isDepartmentSave = this.save(department);
        if (isDepartmentSave) {
            return R.ok("外部单位新增成功！");
        } else {
            return R.error(ResponseCodeEnum.SERVER_ERROR, "外部单位新增失败！");
        }
    }

    /**
     * 删除外部单位
     * @param id
     * @return R 成功或失败信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteDepartment(Long id) {

//        Department department = this.getById(id);
//
//        //如果单位不存在
//        if (department == null) {
//            log.error("所需删除的单位不存在");
//            return R.error(ResponseCodeEnum.NOT_FOUND, "所需删除的单位不存在");
//        }

        /*
        从department表中删掉
         */
        boolean isDepartmentDelete = this.removeById(id);
        if (isDepartmentDelete) {
            return R.ok("外部单位删除成功！");
        } else {
            return R.error(ResponseCodeEnum.SERVER_ERROR, "外部单位删除失败！");
        }

    }

    /**
     * 修改外部单位信息
     * @param id
     * @param departmentDto
     * @return R 修改结果成功或失败信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateDepartment(Long id, DepartmentDto departmentDto) {

        Department department = this.getById(id);

        //如果不存在
        if (department == null) {
            log.error("所需修改的单位不存在");
            return R.error(ResponseCodeEnum.NOT_FOUND, "所需修改的单位不存在");
        }
        //更新
        department.setName(departmentDto.name());
        department.setPosition(departmentDto.position());
        department.setPayType(departmentDto.payType());
        department.setIsInterior(departmentDto.isInterior());
        department.setPositionLongitude(Double.parseDouble(departmentDto.positionLongitude()));
        department.setPositionLatitude(Double.parseDouble(departmentDto.positionLatitude()));

        /*
        修改外部单位信息，存数据库
         */
        boolean isDepartmentUpdate = this.updateById(department);
        if (isDepartmentUpdate) {
            return R.ok("外部单位信息修改成功！");
        } else {
            return R.error(ResponseCodeEnum.SERVER_ERROR, "外部单位信息修改失败！");
        }
    }

    /**
     * 查询外部单位信息列表
     * @param departmentPageQueryDto
     * @return R 外部单位列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getDepartmentList(DepartmentPageQueryDto departmentPageQueryDto) {

        try {
            IPage<Department> page = new Page<>(
                    departmentPageQueryDto.pageNum(), departmentPageQueryDto.pageSize());

            //query和经纬度查询
            //query查询
            if(StringUtils.hasText(departmentPageQueryDto.query())) {
                this.page(page, new LambdaQueryWrapper<Department>().
                        like(Department::getName, departmentPageQueryDto.query())
                        .or()
                        .like(Department::getPosition, departmentPageQueryDto.query()));

            } else if(StringUtils.hasText(departmentPageQueryDto.latitude())
                    && StringUtils.hasText(departmentPageQueryDto.longitude())) {
                //经纬度查询
                this.page(page, new LambdaQueryWrapper<Department>()
                        .eq(Department::getPositionLatitude, Double.parseDouble(departmentPageQueryDto.latitude()))
                        .eq(Department::getPositionLongitude, Double.parseDouble(departmentPageQueryDto.longitude())));

            } else {
                this.page(page, null);
            }

            //封装
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", page.getTotal());
            resultMap.put("list", page.getRecords());

            return R.ok().put("result", resultMap);
        } catch (Exception e) {
            throw new ApartmentException("查询外部单位列表失败！");
        }
    }

    /**
     * 查询外部单位信息简表
     * @return 简表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getDepartmentSimpleList() {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            /*
            查询
             */
            List<Department> departmentSimpleList = departmentMapper.selectList(
                    new LambdaQueryWrapper<Department>()
                            .select(Department::getId, Department::getName));

            /*
            遍历departmentSimpleList中的每个department并转换为map加入list
             */
            for (Department department : departmentSimpleList) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", department.getName());
                map.put("id", department.getId());
                list.add(map);
            }
            resultMap.put("list", list);
            return R.ok().put("result", resultMap);

        } catch (Exception e) {
            throw new ApartmentException("查询外部单位简表失败！");
        }
    }

    /**
     * 查看外部单位详细信息
     * @param id
     * @return R
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getDepartmentDetail(Long id) {

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        Department department = this.getById(id);
        if (department == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "外部单位不存在");
        }

        //查询该单位管理员列表
        List<Admin> adminList = adminMapper.selectList(
                new LambdaQueryWrapper<Admin>()
                        .select(Admin::getId, Admin::getName)
                        .eq(Admin::getDepartmentId, department.getId()));

        /*
        遍历adminList中的每个admin并转换为map加入list
         */
        for (Admin admin : adminList) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", admin.getName());
            map.put("id", admin.getId());
            list.add(map);
        }

        resultMap.put("department", department);
        resultMap.put("admins", list);
        return R.ok().put("result", resultMap);

    }
}