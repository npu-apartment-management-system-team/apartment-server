package edu.npu.service;

import edu.npu.entity.AccountUserDetails;
import edu.npu.vo.R;

public interface UserLivingMessageService {

    /**
     * 获取用户住宿信息
     * @param accountUserDetails
     * @return R
     */
    R getUserLivingMessage(AccountUserDetails accountUserDetails);
}
