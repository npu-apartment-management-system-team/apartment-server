package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.UserApplicationDto;
import edu.npu.dto.UserStatusPageQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.Application;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

/**
* @author wangminan
* @description 针对表【application(申请表)】的数据库操作Service
* @createDate 2023-06-29 21:18:43
*/
public interface UserApplicationService extends IService<Application> {

    R getApplicationStatus(AccountUserDetails accountUserDetails,
                           UserStatusPageQueryDto pageQueryDto);

    @Transactional(rollbackFor = Exception.class)
    R handleSaveUserApplication(AccountUserDetails accountUserDetails, UserApplicationDto userApplicationDto);

    @Transactional(rollbackFor = Exception.class)
    R handleWithdrawApplication(AccountUserDetails accountUserDetails, Integer id);
}
