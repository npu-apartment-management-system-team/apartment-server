package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ApplicationStatusEnum;
import edu.npu.entity.Application;
import edu.npu.entity.ProcessingApplication;
import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.ApplicationMapper;
import edu.npu.service.ProcessingApplicationService;
import edu.npu.mapper.ProcessingApplicationMapper;
import edu.npu.util.SendMailUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static edu.npu.common.ApplicationStatusEnum.*;

/**
* @author wangminan
* @description 针对表【processing_application(正在进行中的申请表)】的数据库操作Service实现
* @createDate 2023-07-02 11:10:29
*/
@Service
@Slf4j
public class ProcessingApplicationServiceImpl extends ServiceImpl<ProcessingApplicationMapper, ProcessingApplication>
    implements ProcessingApplicationService{

    @Resource
    private ProcessingApplicationMapper processingApplicationMapper;

    @Resource
    private ApplicationMapper applicationMapper;

    @Resource
    private SendMailUtil sendMailUtil;

    @Resource
    private UserServiceClient userServiceClient;

    @Override
    public boolean handleExpireApplication(Long shardIndex, int shardTotal, int count) {
        log.info("收到调度,开始执行定时任务,当前分片:{}", shardIndex);
        List<ProcessingApplication> processingApplications =
            processingApplicationMapper.getListByShardIndex(
                shardIndex, shardTotal, count);
        if (processingApplications.isEmpty()) {
            log.info("当前任务没有需要处理的Application,直接返回");
            return true;
        }
        log.info("取出:{}条数据,开始执行轮询操作",processingApplications.size());
        for (ProcessingApplication processingApplication : processingApplications) {
            Application application = applicationMapper.selectById(
                processingApplication.getApplicationId());
            if (application == null) {
                log.error("脏数据,申请表中不存在该申请,申请id:{}",
                        processingApplication.getApplicationId());
                continue;
            }
            if (statusList.contains(ApplicationStatusEnum.fromValue(
                    application.getApplicationStatus()))) {
                log.info("申请id:{}的申请状态为:{},开始执行过期操作",
                        application.getId(), application.getApplicationStatus());
                // updateTime距离当前时间超过48小时 向用户发送邮件
                if (System.currentTimeMillis() - application.getUpdateTime().getTime()
                        > 48 * 60 * 60 * 1000) {
                    log.info("申请id:{}的申请状态为:{},已经超过48小时,向用户发送邮件",
                            application.getId(), application.getApplicationStatus());
                    sendNoticeMail(application);
                } else if (System.currentTimeMillis() - application.getUpdateTime().getTime()
                        > 72 * 60 * 60 * 1000) {
                    // 超过72小时
                    log.info("申请id:{}的申请状态为:{},已经超过72小时,执行过期操作",
                            application.getId(), application.getApplicationStatus());
                    expireApplication(application);
                    int updateApplication = applicationMapper.updateById(application);
                    // 删除processingApplication
                    int deleteProcessingApplication =
                            processingApplicationMapper.deleteById(processingApplication.getId());
                    if (updateApplication != 1 || deleteProcessingApplication != 1) {
                        log.error("申请id:{}的申请状态为:{},执行过期操作失败",
                                application.getId(), application.getApplicationStatus());
                    }
                }
            } else {
                log.info("申请id:{}的申请状态为:{},不需要执行过期操作",
                        application.getId(), application.getApplicationStatus());
            }
        }
        log.info("完成任务执行");
        return true;
    }

    private static final List<ApplicationStatusEnum> statusList = List.of(
            CENTER_DORM_ALLOCATION,
            CHECK_IN_DEPOSIT,
            CENTER_DORM_CHANGE_ALLOCATION,
            CENTER_DORM_MANAGER_CHANGE_CHECK_OUT_CONFIRM,
            CHECK_OUT_SUBMIT
    );

    private void expireApplication(Application application) {
        if (application.getApplicationStatus().equals(
                CENTER_DORM_ALLOCATION.getValue()
        )) {
            application.setApplicationStatus(CHECK_IN_DEPOSIT_TIMEOUT.getValue());
        } else if (application.getApplicationStatus().equals(
                CHECK_IN_DEPOSIT.getValue()
        )) {
            application.setApplicationStatus(
                    CENTER_DORM_MANAGER_CHECK_IN_CONFIRM_TIMEOUT.getValue());
        } else if (application.getApplicationStatus().equals(
                CENTER_DORM_CHANGE_ALLOCATION.getValue()
        )) {
            application.setApplicationStatus(
                    CENTER_DORM_MANAGER_CHANGE_CHECK_OUT_CONFIRM_TIMEOUT.getValue()
            );
        } else if (application.getApplicationStatus().equals(
                CENTER_DORM_MANAGER_CHANGE_CHECK_OUT_CONFIRM.getValue()
        )) {
            application.setApplicationStatus(
                    CENTER_DORM_MANAGER_CHANGE_CHECK_IN_CONFIRM_TIMEOUT.getValue()
            );
        } else if (application.getApplicationStatus().equals(
                CHECK_OUT_SUBMIT.getValue()
        )){
            // 退宿超时
            application.setApplicationStatus(
                    CENTER_DORM_MANAGER_CHECK_OUT_CONFIRM_TIMEOUT.getValue()
            );
        }
    }

    private void sendNoticeMail(Application application) {
        // 发送提醒邮件
        User user = userServiceClient.getUserById(
                application.getUserId()
        );
        if (user != null) {
            String email = user.getEmail();
            if (email != null) {
                String subject = "杭房段宿舍管理申请进度提醒";
                String content =
                        "您的编号为: "+ application.getId() +
                        " 的申请已经超过48小时未更新进度。" +
                        "该申请将在最后一次更新后72小时超时,请及时处理,谢谢!";
                sendMailUtil.sendMail(
                        email, subject, content
                );
            }
        }
    }
}




