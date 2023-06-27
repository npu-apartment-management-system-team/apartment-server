package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.AlipayLoginCallbackDto;
import edu.npu.dto.CheckSmsCodeDto;
import edu.npu.dto.UserLoginDto;
import edu.npu.dto.UserRegisterDto;
import edu.npu.entity.LoginAccount;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【login_account(登录账号)】的数据库操作Service
* @createDate 2023-06-26 21:19:29
*/
public interface LoginAccountService extends IService<LoginAccount> {

    String handleAlipayLogin(AlipayLoginCallbackDto alipayLoginCallbackDto);

    R login(UserLoginDto userLoginDto);

    R registerUser(UserRegisterDto userRegisterDto);

    R loginByPhone(CheckSmsCodeDto checkSmsCodeDto);

    R logout(LoginAccount loginAccount);
}
