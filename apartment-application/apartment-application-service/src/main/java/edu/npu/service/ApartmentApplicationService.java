package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.BasicPageQueryDto;
import edu.npu.dto.BasicReviewDto;
import edu.npu.entity.Application;
import edu.npu.vo.R;

public interface ApartmentApplicationService extends IService<Application> {
    R getApplicationList(BasicPageQueryDto basicPageQueryDto);

    R statusChangeConfirm(BasicReviewDto reviewDto);
}
