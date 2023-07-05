package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.AdminDto;
import edu.npu.dto.AdminPageQueryDto;
import edu.npu.entity.Admin;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

/**
* @author wangminan
* @description 针对表【admin(管理员)】的数据库操作Service
* @createDate 2023-06-27 21:19:32
*/
public interface AdminService extends IService<Admin> {

    /**
     * 新增管理员账号
     * @param adminDto admin账号信息
     * @return R 新增结果
     */
    @Transactional(rollbackFor = Exception.class)
    R addAdmin(AdminDto adminDto);

    /**
     * 删除管理员账号
     * @param id 管理员id
     * @return R 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    R deleteAdmin(Long id);

    /**
     * 修改管理员账号
     * @param id 管理员id
     * @return 修改结果
     */
    @Transactional(rollbackFor = Exception.class)
    R updateAdmin(Long id, AdminDto adminDto);

    /**
     * 查询管理员账号列表
     * @param adminPageQueryDto 查询条件
     * @return 列表
     */
    @Transactional(rollbackFor = Exception.class)
    R getAdminList(AdminPageQueryDto adminPageQueryDto);

    /**
     * 查询班组长列表
     * @return 班组长列表
     */
    @Transactional(rollbackFor = Exception.class)
    R getForemanList();
}
