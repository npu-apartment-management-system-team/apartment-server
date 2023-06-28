package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.UpdatePasswordDto;
import edu.npu.entity.LoginAccount;
import edu.npu.vo.R;

public interface CommonService extends IService<LoginAccount> {
    R updatePassword(Long id, UpdatePasswordDto updatePasswordDto);
}
