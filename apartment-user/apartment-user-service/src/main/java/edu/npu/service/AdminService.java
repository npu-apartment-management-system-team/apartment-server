package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.AddAdminDto;
import edu.npu.dto.PageQueryDto;
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
    R addAdmin(AddAdminDto addAdminDto);

    @Transactional(rollbackFor = Exception.class)
    R deleteAdmin(Integer id);

    @Transactional(rollbackFor = Exception.class)
    R updateAdmin(Integer id);

    @Transactional(rollbackFor = Exception.class)
    R getAdminList(PageQueryDto pageQueryDto);

    @Transactional(rollbackFor = Exception.class)
    R getForemanList();
}
