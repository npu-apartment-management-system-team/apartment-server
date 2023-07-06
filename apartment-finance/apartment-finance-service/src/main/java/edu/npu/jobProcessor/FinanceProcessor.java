package edu.npu.jobProcessor;

import com.alibaba.schedulerx.worker.domain.JobContext;
import com.alibaba.schedulerx.worker.processor.JavaProcessor;
import com.alibaba.schedulerx.worker.processor.ProcessResult;
import edu.npu.exception.ApartmentException;
import edu.npu.feignClient.ManagementServiceClient;
import edu.npu.service.GeneratePaymentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : [wangminan]
 * @description : [对接schedulerX2的处理类 每月定时开单]
 */
@Slf4j
@Component
public class FinanceProcessor extends JavaProcessor {

    @Resource
    private GeneratePaymentService generatePaymentService;

    @Resource
    private ManagementServiceClient managementServiceClient;

    private static final ExecutorService cachedThreadPool =
        Executors.newFixedThreadPool(
            // 获取系统核数
            Runtime.getRuntime().availableProcessors() * 2
        );

    private static final Random random;

    static {
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new ApartmentException(e.getMessage());
        }
    }

    /**
     * 所有机器会执行
     */
    @Override
    public ProcessResult process(JobContext context) {
        int value = random.nextInt(10);
        log.info("接收到来自schedulerX2的定时调度任务,开始执行确认申请过期的定时任务");
        Long shardIndex = context.getShardingId();
        int shardTotal = context.getShardingNum();
        Long taskId = context.getTaskId();
        log.info("定时任务参数:shardIndex={},shardTotal={},taskId={}",
                shardIndex,shardTotal,taskId);
        // 开启两个线程 第一个线程生成部门缴费订单 第二个线程生成用户缴费订单
        cachedThreadPool.execute(() -> {
            log.info("开始生成部门缴费订单");
            generatePaymentService.generateDepartmentPayment(shardIndex,shardTotal);
        });
        cachedThreadPool.execute(() -> {
            log.info("开始生成用户缴费订单");
            generatePaymentService.generateUserPayment(shardIndex,shardTotal);
        });
        return new ProcessResult(true, String.valueOf(value));
    }

}
