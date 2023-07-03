package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ApplicationStatusEnum;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.UserStatusEnum;
import edu.npu.dto.BasicPageQueryDto;
import edu.npu.dto.BasicReviewDto;
import edu.npu.entity.Application;
import edu.npu.entity.ProcessingApplication;
import edu.npu.entity.User;
import edu.npu.feignClient.FinanceServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.ApplicationMapper;
import edu.npu.mapper.ProcessingApplicationMapper;
import edu.npu.service.ApartmentApplicationService;
import edu.npu.vo.R;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static edu.npu.common.ApplicationStatusEnum.*;

/**
 * @author : [wangminan]
 * @description : [针对表【application(申请表)】的数据库操作Service实现]
 */
@Service
@Slf4j
public class ApartmentApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
        implements ApartmentApplicationService {

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private ProcessingApplicationMapper processingApplicationMapper;

    @Resource
    private FinanceServiceClient financeServiceClient;

    @Override
    public R getApplicationList(BasicPageQueryDto basicPageQueryDto) {
        Page<Application> page = new Page<>(
                basicPageQueryDto.pageNum(), basicPageQueryDto.pageSize());
        LambdaQueryWrapper<Application> wrapper =
            new LambdaQueryWrapper<Application>()
                // 入住
                .eq(Application::getApplicationStatus,
                    CHECK_IN_DEPOSIT.getValue())
                .or()
                // 调宿
                .eq(Application::getApplicationStatus,
                    CENTER_DORM_CHANGE_ALLOCATION.getValue())
                .or()
                .eq(Application::getApplicationStatus,
                    ApplicationStatusEnum.
                        CENTER_DORM_MANAGER_CHANGE_CHECK_OUT_CONFIRM.getValue())
                // 退宿
                .eq(Application::getApplicationStatus,
                    ApplicationStatusEnum.CHECK_OUT_SUBMIT.getValue())
                .orderByDesc(Application::getUpdateTime);
        page = page(page, wrapper);

        Map<String, Object> result = Map.of(
            "total", page.getTotal(),
            "list", page.getRecords()
        );

        return R.ok(result);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public R statusChangeConfirm(BasicReviewDto reviewDto) {
        Application application = getById(reviewDto.id());
        User user = userServiceClient.getUserById(application.getUserId());
        boolean updateUser = true;
        if (application.getApplicationStatus().equals(CHECK_IN_DEPOSIT.getValue())){
            application.setApplicationStatus(CHECK_IN_COMPLETE.getValue());
            user.setStatus(UserStatusEnum.CHECK_IN.getValue());
            updateUser = userServiceClient.updateUser(user);
        } else if (application.getApplicationStatus()
                .equals(CENTER_DORM_CHANGE_ALLOCATION.getValue())) {
            application.setApplicationStatus(CENTER_DORM_MANAGER_CHANGE_CHECK_OUT_CONFIRM.getValue());
        } else if (application.getApplicationStatus()
                .equals(CENTER_DORM_MANAGER_CHANGE_CHECK_OUT_CONFIRM.getValue())) {
            application.setApplicationStatus(CHANGE_DORM_COMPLETE.getValue());
            user.setStatus(UserStatusEnum.CHECK_IN.getValue());
            updateUser = userServiceClient.updateUser(user);
        } else if(application.getApplicationStatus().equals(CHECK_OUT_SUBMIT.getValue())) {
            application.setApplicationStatus(CHECK_OUT_COMPLETE.getValue());
            user.setStatus(UserStatusEnum.NOT_CHECK_IN.getValue());
            updateUser = userServiceClient.updateUser(user);

            boolean refund = financeServiceClient.refundDepositCharge(user.getId());
            if (!refund) {
                log.error("退宿退款失败,用户id:{}", user.getId().toString());
            }

        } else {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "申请状态错误");
        }
        application.setUpdateTime(new Date(System.currentTimeMillis()));

        // 更新processing_application表
        int removeProcessing = processingApplicationMapper.delete(
                new LambdaQueryWrapper<ProcessingApplication>()
                        .eq(ProcessingApplication::getApplicationId, application.getId())
        );

        return removeProcessing == 1 && updateById(application) && updateUser ?
            R.ok() :
            R.error(ResponseCodeEnum.SERVER_ERROR, "数据库异常,申请状态更新失败");
    }
}
