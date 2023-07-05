package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ApplicationStatusEnum;
import edu.npu.common.BedInUseEnum;
import edu.npu.common.IsForCadreEnum;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.AllocationDto;
import edu.npu.dto.BasicPageQueryDto;
import edu.npu.dto.BasicReviewDto;
import edu.npu.entity.*;
import edu.npu.feignClient.FinanceServiceClient;
import edu.npu.feignClient.ManagementServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.ApplicationMapper;
import edu.npu.mapper.ProcessingApplicationMapper;
import edu.npu.service.CenterApplicationService;
import edu.npu.util.SendMailUtil;
import edu.npu.vo.R;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static edu.npu.common.ApplicationStatusEnum.*;

/**
 * @author : [wangminan]
 * @description : [杭房段申请服务实现类]
 */
@Service
public class CenterApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
        implements CenterApplicationService {

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private ManagementServiceClient managementServiceClient;

    @Resource
    private ProcessingApplicationMapper processingApplicationMapper;

    @Resource
    private SendMailUtil sendMailUtil;

    @Resource
    private FinanceServiceClient financeServiceClient;

    private static final ExecutorService SEND_MAIL_THREAD_POOL =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors());

    @Override
    public R getApplicationListForCenter(
            BasicPageQueryDto basicPageQueryDto) {
        return getListWithStatus(
                basicPageQueryDto,
                DEPARTMENT_CHECK_IN_APPROVAL, DEPARTMENT_CHANGE_DORM_APPROVAL
        );
    }

    /**
     * 杭房段审核入住/换寝申请
     *
     * @param basicReviewDto 审核信息
     * @return R
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R reviewApplicationForCenter(
            BasicReviewDto basicReviewDto) {
        Application application = getById(basicReviewDto.id());
        if (application == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "申请表不存在");
        }
        if (basicReviewDto.pass()) {
            application.setApplicationStatus(
                    application.getApplicationStatus() + 1
            );
            SEND_MAIL_THREAD_POOL.execute(() -> sendNoticeMail(application, true));
        } else {
            if (application.getApplicationStatus() ==
                    DEPARTMENT_CHECK_IN_APPROVAL.getValue()) {
                // 入住
                application.setApplicationStatus(
                        CENTER_CHECK_IN_REJECT.getValue()
                );
            } else {
                // 调宿
                application.setApplicationStatus(
                        CENTER_CHANGE_DORM_REJECT.getValue()
                );
            }
            // 从待处理申请表中删除
            processingApplicationMapper.delete(
                    new LambdaQueryWrapper<ProcessingApplication>()
                            .eq(ProcessingApplication::getApplicationId,
                                    application.getId())
            );
            SEND_MAIL_THREAD_POOL.execute(() -> sendNoticeMail(application, false));
        }
        application.setUpdateTime(new Date(System.currentTimeMillis()));
        return updateById(application) ? R.ok() :
                R.error(ResponseCodeEnum.SERVER_ERROR, "数据库异常,更新申请状态失败");
    }

    /**
     * 获取等待分配床位的申请列表
     *
     * @param basicPageQueryDto 分页信息
     * @return R
     */
    @Override
    public R getWaitingAllocateList(
            BasicPageQueryDto basicPageQueryDto) {
        return getListWithStatus(
                basicPageQueryDto,
                CENTER_CHECK_IN_APPROVAL, CENTER_CHANGE_DORM_APPROVAL
        );
    }

    @Override
    // 启用分布式事务
    @GlobalTransactional(rollbackFor = Exception.class)
    public R handleAllocateBed(AllocationDto allocationDto) {
        // 大事务 需要同时调用实体管理更新床位信息 还需要更新申请表 更新user信息
        // 数据预校验
        Application application = getById(allocationDto.id());
        User user = userServiceClient.getUserById(
                application.getUserId()
        );
        Bed bed = managementServiceClient.getBedById(
                allocationDto.bedId()
        );
        if (bed.getIsInUse().equals(BedInUseEnum.IN_USE.getValue())) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "床位已被占用");
        }
        Room room = managementServiceClient.getRoomById(
                bed.getRoomId()
        );
        if ((room.getIsForCadre().equals(IsForCadreEnum.IS_FOR_CADRE.getValue()) &&
                user.getIsCadre().equals(IsForCadreEnum.NOT_FOR_CADRE.getValue())) ||
                (room.getIsForCadre().equals(IsForCadreEnum.NOT_FOR_CADRE.getValue()) &&
                        user.getIsCadre().equals(IsForCadreEnum.IS_FOR_CADRE.getValue()))) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "分配床位与住宿职工职级不符");
        }
        List<User> userList = userServiceClient.getUserByBedId(bed.getId());
        if (!userList.isEmpty()) {
            for (User tmpUser : userList) {
                if (!tmpUser.getSex().equals(user.getSex())) {
                    return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                            "分配床位与住宿职工性别不符");
                }
            }
        }

        // 更新申请表
        application.setApplicationStatus(
                application.getApplicationStatus() + 1
        );
        application.setUpdateTime(new Date(System.currentTimeMillis()));
        boolean updateApplication = updateById(application);

        // 更新床位信息
        bed.setIsInUse(BedInUseEnum.IN_USE.getValue());
        boolean updateBed = managementServiceClient.updateBed(bed);

        // 更新user信息
        user.setBedId(bed.getId());
        boolean updateUser = userServiceClient.updateUser(user);

        boolean addDepositCharge =
                financeServiceClient.addDepositCharge(user.getId());

        if (addDepositCharge && updateApplication && updateBed && updateUser) {
            SEND_MAIL_THREAD_POOL.execute(() -> {
                String email = user.getEmail();
                if (email != null) {
                    String subject = "杭房段宿舍管理申请进度提醒";
                    String content =
                            "杭房段已为您分配宿舍，您现在可以登录平台查看。请及时缴纳押金。";
                    sendMailUtil.sendMail(
                            email, subject, content
                    );
                }
            });
            return R.ok();
        }
        return R.error(ResponseCodeEnum.SERVER_ERROR, "数据库异常,更新申请状态失败");
    }

    private void sendNoticeMail(Application application, Boolean pass) {
        // 发送提醒邮件
        User user = userServiceClient.getUserById(
                application.getUserId()
        );
        if (user != null) {
            String email = user.getEmail();
            if (email != null) {
                String subject = "杭房段宿舍管理申请进度提醒";
                String content =
                        "您的" +
                                (application.getApplicationStatus() ==
                                        CHECK_IN_SUBMIT.getValue() ?
                                        "入住" : "调宿") +
                                "申请在杭房段" +
                                (Boolean.TRUE.equals(pass) ? "已通过审核" : "被驳回") +
                                "，请及时查看";
                sendMailUtil.sendMail(
                        email, subject, content
                );
            }
        }
    }

    private R getListWithStatus(
            BasicPageQueryDto basicPageQueryDto,
            ApplicationStatusEnum statusEnum1,
            ApplicationStatusEnum statusEnum2) {
        Page<Application> page = new Page<>(
                basicPageQueryDto.pageNum(), basicPageQueryDto.pageSize());
        LambdaQueryWrapper<Application> wrapper =
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getApplicationStatus, statusEnum1.getValue())
                        .or()
                        .eq(Application::getApplicationStatus, statusEnum2.getValue())
                        .orderByDesc(Application::getUpdateTime);
        page = page(page, wrapper);
        Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords()
        );
        return R.ok(result);
    }
}
