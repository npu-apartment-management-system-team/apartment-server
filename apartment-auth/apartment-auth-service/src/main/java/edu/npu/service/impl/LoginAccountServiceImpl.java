package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.LoginAccount;
import edu.npu.service.LoginAccountService;
import edu.npu.mapper.LoginAccountMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【login_account(登录账号)】的数据库操作Service实现
* @createDate 2023-06-26 21:19:29
*/
@Service
public class LoginAccountServiceImpl extends ServiceImpl<LoginAccountMapper, LoginAccount>
    implements LoginAccountService{

}




