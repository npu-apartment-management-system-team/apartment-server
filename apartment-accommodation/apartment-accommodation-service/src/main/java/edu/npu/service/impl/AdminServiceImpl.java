package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Admin;
import edu.npu.mapper.AdminMapper;
import edu.npu.service.AdminService;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【admin(管理员)】的数据库操作Service实现
* @createDate 2023-06-29 21:18:43
*/
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{

}




