package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.UpdatePasswordDto;
import edu.npu.entity.LoginAccount;
import edu.npu.mapper.LoginAccountMapper;
import edu.npu.service.CommonService;
import edu.npu.util.RsaUtil;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class CommonServiceImpl extends ServiceImpl<LoginAccountMapper, LoginAccount>
    implements CommonService {

    @Value("${var.rsa.private-key}")
    private String privateKey;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public R updatePassword(Long id, UpdatePasswordDto updatePasswordDto) {
        LoginAccount loginAccount=getById(id);
        String oldPasswordFromOnt = RsaUtil.decrypt(privateKey,updatePasswordDto.oldPassword());
        if (!passwordEncoder.matches(oldPasswordFromOnt, loginAccount.getPassword())) {
            return R.error(ResponseCodeEnum.CREATION_ERROR, "旧密码不匹配");
        }
        String newPasswordFromOnt = RsaUtil.decrypt(privateKey,updatePasswordDto.newPassword());
        loginAccount.setPassword(passwordEncoder.encode(newPasswordFromOnt));
        updateById(loginAccount);
        return R.ok();
    }
}
