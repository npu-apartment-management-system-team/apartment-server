package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.*;
import edu.npu.dto.UserApplicationDto;
import edu.npu.dto.UserStatusPageQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.Application;
import edu.npu.entity.ProcessingApplication;
import edu.npu.entity.User;
import edu.npu.exception.ApartmentError;
import edu.npu.exception.ApartmentException;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.ApplicationMapper;
import edu.npu.mapper.ProcessingApplicationMapper;
import edu.npu.service.UserApplicationService;
import edu.npu.vo.R;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static edu.npu.common.ApplicationStatusEnum.*;
import static edu.npu.common.ApplicationTypeEnum.CHANGE_DORM;

/**
 * @author wangminan
 * @description 针对表【application(申请表)】的数据库操作Service实现
 * @createDate 2023-06-29 21:18:43
 */
@Service
public class UserApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
        implements UserApplicationService {

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private ProcessingApplicationMapper processingApplicationMapper;

    @Override
    public R getApplicationStatus(AccountUserDetails accountUserDetails,
                                  UserStatusPageQueryDto pageQueryDto) {
        preCheckAccountForUser(accountUserDetails);
        User user = getUserFromAccountUserDetails(accountUserDetails);

        Page<Application> page = new Page<>(
                pageQueryDto.pageNum(), pageQueryDto.pageSize());
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getUserId, user.getId())
                .orderByDesc(Application::getUpdateTime);
        page = page(page, wrapper);

        Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords()
        );

        return R.ok(result);
    }

    /**
     * 处理用户提交申请
     * @param accountUserDetails 用户信息
     * @param userApplicationDto 申请信息
     * @return R
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public R handleSaveUserApplication(
            AccountUserDetails accountUserDetails,
            UserApplicationDto userApplicationDto) {
        preCheckAccountForUser(accountUserDetails);
        if (ApplicationTypeEnum.fromValue(userApplicationDto.type()) == null) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "申请类型不存在");
        }
        User user = getUserFromAccountUserDetails(accountUserDetails);

        // 查表 如果该用户有进行中的申请则不允许新增
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getUserId, user.getId())
                .between(Application::getApplicationStatus,
                        CHECK_IN_SUBMIT.getValue(),
                        CHECK_IN_DEPOSIT.getValue())
                .or()
                .between(Application::getApplicationStatus,
                        CHANGE_DORM_SUBMIT.getValue(),
                        CENTER_DORM_MANAGER_CHANGE_CHECK_OUT_CONFIRM.getValue())
                .or()
                .eq(Application::getApplicationStatus,
                        CHECK_OUT_SUBMIT.getValue());
        if (count(wrapper) > 0) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "您有进行中的申请,请勿重复申请");
        }

        ApplicationStatusEnum status;
        if (ApplicationTypeEnum.fromValue(userApplicationDto.type()) ==
                ApplicationTypeEnum.CHECK_IN) {
            status = ApplicationStatusEnum.CHECK_IN_SUBMIT;
        } else if (ApplicationTypeEnum.fromValue(userApplicationDto.type()) ==
                CHANGE_DORM) {
            status = ApplicationStatusEnum.CHANGE_DORM_SUBMIT;
        } else {
            status = ApplicationStatusEnum.CHECK_OUT_SUBMIT;
        }

        Application application = Application.builder()
                .userId(user.getId())
                .type(userApplicationDto.type())
                .fileUrl(userApplicationDto.fileUrl())
                .applicationStatus(status.getValue())
                .createTime(new Date(System.currentTimeMillis()))
                .updateTime(new Date(System.currentTimeMillis()))
                .build();

        // 更新用户状态到申请中
        user.setStatus(UserStatusEnum.CHECK_IN_APPLICATION.getValue());
        boolean updateUser = userServiceClient.updateUser(user);

        // 更新申请到申请表
        boolean saveApplication = save(application);

        // 更新用户申请信息到processing_application表
        ProcessingApplication processingApplication = new ProcessingApplication();
        processingApplication.setApplicationId(application.getId());
        int saveProcessingApplication = processingApplicationMapper.insert(processingApplication);

        return saveApplication && updateUser && saveProcessingApplication == 1 ?
                R.ok() : R.error(
                ResponseCodeEnum.CREATION_ERROR, "数据库问题,创建申请失败"
        );
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public R handleWithdrawApplication(AccountUserDetails accountUserDetails, Integer id) {
        preCheckAccountForUser(accountUserDetails);
        User user = getUserFromAccountUserDetails(accountUserDetails);
        Application application = getById(id);
        if (!application.getUserId().equals(user.getId())) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "该申请不属于该用户");
        }
        ApplicationStatusEnum status;
        if (ApplicationTypeEnum.fromValue(application.getType()) ==
                ApplicationTypeEnum.CHECK_IN) {
            // 正在入住执行流程中 预检 是否满足撤回条件
            if (application.getDepositStatus().equals(DepositStatusEnum.PAID.getValue())) {
                return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                        "已缴纳押金,请在入住后随即办理退宿手续");
            }
            if (application.getApplicationStatus() == CHECK_IN_WITHDRAW.getValue()) {
                return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                        "已撤回,请勿重复操作");
            }
            if (application.getApplicationStatus() >= CENTER_DORM_ALLOCATION.getValue() ||
                    application.getApplicationStatus() == CHECK_IN_COMPLETE.getValue()) {
                return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                        "已超过可撤回阶段,请等待流程完成后重新申请");
            }
            user.setStatus(UserStatusEnum.NOT_CHECK_IN.getValue());
            status = CHECK_IN_WITHDRAW;
        } else if (ApplicationTypeEnum.fromValue(application.getType()) ==
                CHANGE_DORM) {
            // 正在执行流程中 预检 是否满足撤回条件
            if (application.getApplicationStatus() == CHANGE_DORM_WITHDRAW.getValue()) {
                return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                        "已撤回,请勿重复操作");
            }
            if (application.getApplicationStatus() >= CENTER_DORM_CHANGE_ALLOCATION.getValue() ||
                    application.getApplicationStatus() == CHANGE_DORM_COMPLETE.getValue()) {
                return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                        "已超过可撤回阶段,请等待流程完成后重新申请");
            }
            user.setStatus(UserStatusEnum.CHECK_IN.getValue());
            status = CHANGE_DORM_WITHDRAW;
        } else {
            // 正在执行流程中 预检 是否满足撤回条件
            if (application.getApplicationStatus() != CHECK_OUT_SUBMIT.getValue()) {
                return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                        "非可撤回阶段,请重新提交申请");
            }
            user.setStatus(UserStatusEnum.CHECK_IN.getValue());
            status = CHECK_OUT_WITHDRAW;
        }
        application.setApplicationStatus(status.getValue());
        application.setUpdateTime(new Date(System.currentTimeMillis()));

        // 从processing_application表中删除
        int removeProcessing = processingApplicationMapper.delete(
                new LambdaQueryWrapper<ProcessingApplication>()
                        .eq(ProcessingApplication::getApplicationId, application.getId())
        );

        boolean updateUser = userServiceClient.updateUser(user);

        return removeProcessing == 1 && updateById(application) && updateUser ?
                R.ok() : R.error(
                ResponseCodeEnum.SERVER_ERROR, "数据库问题,更新申请失败"
        );
    }

    private User getUserFromAccountUserDetails(AccountUserDetails accountUserDetails) {
        User user = userServiceClient.getUserByLoginAccountId(
                accountUserDetails.getId());
        if (user == null) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "用户不存在,请重试");
        }
        return user;
    }

    private void preCheckAccountForUser(AccountUserDetails accountUserDetails) {
        if (accountUserDetails.getId() == null) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "登录用户id为空");
        }
        if (accountUserDetails.getRole() != RoleEnum.USER.getValue()) {
            throw new ApartmentException(ApartmentError.OBJECT_NULL, "用户角色不是住宿职工");
        }
    }
}
