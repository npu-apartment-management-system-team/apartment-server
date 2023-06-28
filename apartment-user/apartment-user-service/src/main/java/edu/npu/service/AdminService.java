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

    @Transactional(rollbackFor = Exception.class)
    R addAdmin(AdminDto adminDto);

    @Transactional(rollbackFor = Exception.class)
    R deleteAdmin(Long id);

    @Transactional(rollbackFor = Exception.class)
    R updateAdmin(Long id, AdminDto adminDto);

    @Transactional(rollbackFor = Exception.class)
    R getAdminList(AdminPageQueryDto adminPageQueryDto);

    @Transactional(rollbackFor = Exception.class)
    R getForemanList();
}
