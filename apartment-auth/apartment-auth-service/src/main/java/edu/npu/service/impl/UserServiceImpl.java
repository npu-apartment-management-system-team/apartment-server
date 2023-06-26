package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.User;
import edu.npu.service.UserService;
import edu.npu.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【user(住宿职工表)】的数据库操作Service实现
* @createDate 2023-06-26 21:19:29
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




