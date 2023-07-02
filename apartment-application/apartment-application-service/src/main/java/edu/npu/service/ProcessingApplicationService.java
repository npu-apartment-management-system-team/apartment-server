package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.ProcessingApplication;

/**
* @author wangminan
* @description 针对表【processing_application(正在进行中的申请表)】的数据库操作Service
* @createDate 2023-07-02 11:10:29
*/
public interface ProcessingApplicationService extends IService<ProcessingApplication> {

    boolean handleExpireApplication(Long shardIndex, int shardTotal, int i);
}
