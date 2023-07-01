package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.BasicPageQueryDto;
import edu.npu.dto.BasicReviewDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.Application;
import edu.npu.vo.R;

/**
 * 针对表【application(申请表)】的数据库操作Service
 */
public interface DepartmentApplicationService extends IService<Application> {
    R getApplicationListForDepartment(AccountUserDetails accountUserDetails,
                                      BasicPageQueryDto basicPageQueryDto);

    R reviewApplicationForDepartment(AccountUserDetails accountUserDetails,
                                     BasicReviewDto basicReviewDto);
}
