package edu.npu.jobProcessor;

import com.alibaba.schedulerx.worker.domain.JobContext;
import com.alibaba.schedulerx.worker.processor.JavaProcessor;
import com.alibaba.schedulerx.worker.processor.ProcessResult;
import edu.npu.service.ProcessingApplicationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @author : [wangminan]
 * @description : [对接schedulerX2的处理类]
 */
@Component
@Slf4j
public class ApplicationProcessor extends JavaProcessor {

    @Resource
    private ProcessingApplicationService processingApplicationService;

    /**
     * 所有机器会执行
     */
    @Override
    public ProcessResult process(JobContext context) {
        int value = new Random().nextInt(10);
        log.info("接收到来自schedulerX2的定时调度任务,开始执行确认申请过期的定时任务");
        Long shardIndex = context.getShardingId();
        int shardTotal = context.getShardingNum();
        Long taskId = context.getTaskId();
        log.info("定时任务参数:shardIndex={},shardTotal={},taskId={}",
                shardIndex,shardTotal,taskId);
        boolean handleExpireApplication =
            processingApplicationService.handleExpireApplication(shardIndex, shardTotal, 100);
        return new ProcessResult(handleExpireApplication, String.valueOf(value));
    }
}
