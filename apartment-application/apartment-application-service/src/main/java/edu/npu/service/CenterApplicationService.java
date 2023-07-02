package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.AllocationDto;
import edu.npu.dto.BasicPageQueryDto;
import edu.npu.dto.BasicReviewDto;
import edu.npu.entity.Application;
import edu.npu.vo.R;

/**
 * 针对表【application(申请表)】的数据库操作Service
 */
public interface CenterApplicationService extends IService<Application> {
    R getApplicationListForCenter(BasicPageQueryDto basicPageQueryDto);

    R reviewApplicationForCenter(BasicReviewDto basicReviewDto);

    R getWaitingAllocateList(BasicPageQueryDto basicPageQueryDto);

    R handleAllocateBed(AllocationDto allocationDto);
}
