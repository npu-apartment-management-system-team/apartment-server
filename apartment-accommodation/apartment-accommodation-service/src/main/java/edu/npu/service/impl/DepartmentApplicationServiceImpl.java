package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.BasicPageQueryDto;
import edu.npu.dto.BasicReviewDto;
import edu.npu.entity.*;
import edu.npu.feignClient.ManagementServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.ApplicationMapper;
import edu.npu.service.DepartmentApplicationService;
import edu.npu.util.SendMailUtil;
import edu.npu.vo.R;
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
 * @description : [针对表【application(申请表)】的数据库操作Service实现]
 */
@Service
public class DepartmentApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
        implements DepartmentApplicationService {

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private ManagementServiceClient managementServiceClient;

    @Resource
    private SendMailUtil sendMailUtil;

    private static final ExecutorService SEND_MAIL_THREAD_POOL =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors());

    @Override
    public R getApplicationListForDepartment(
            AccountUserDetails accountUserDetails, BasicPageQueryDto basicPageQueryDto) {
        if (accountUserDetails == null) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "无法获取当前登录用户信息");
        }
        Admin admin = userServiceClient.getAdminByLoginAccountId(
                accountUserDetails.getId()
        );
        if (admin == null) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "当前用户非管理员");
        }
        // 根据admin找department 然后根据department找user
        Long departmentId = admin.getDepartmentId();
        Department department = managementServiceClient.getDepartmentById(departmentId);
        if (department == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "当前用户所属部门不存在");
        }
        List<User> userList = userServiceClient.getUserListByDepartmentId(departmentId);
        // 去application表中查询
        Page<Application> page = new Page<>(
                basicPageQueryDto.pageNum(), basicPageQueryDto.pageSize()
        );
        page = page(page,
                new LambdaQueryWrapper<Application>()
                    .in(Application::getUserId, userList.stream().map(
                        User::getId
                    ).toArray())
                    .eq(Application::getApplicationStatus,
                            CHECK_IN_SUBMIT.getValue())
                    .or()
                    .eq(Application::getApplicationStatus,
                            CHANGE_DORM_SUBMIT.getValue())
                    .orderByDesc(Application::getUpdateTime)
                );
        Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords()
        );
        return R.ok(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R reviewApplicationForDepartment(AccountUserDetails accountUserDetails,
                                            BasicReviewDto basicReviewDto) {
        if (accountUserDetails == null) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "无法获取当前登录用户信息");
        }
        Admin admin = userServiceClient.getAdminByLoginAccountId(
                accountUserDetails.getId()
        );
        if (admin == null) {
            return R.error(ResponseCodeEnum.FORBIDDEN, "当前用户非管理员");
        }
        Application application = getById(basicReviewDto.id());
        if (application == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "申请表不存在");
        }
        if (basicReviewDto.pass()) {
            application.setApplicationStatus(
                application.getApplicationStatus() + 1
            );
            SEND_MAIL_THREAD_POOL.execute(() -> {
                sendNoticeMail(application, true);
            });
        } else {
            if (application.getApplicationStatus() == CHECK_IN_SUBMIT.getValue()) {
                // 入住
                application.setApplicationStatus(
                        DEPARTMENT_CHECK_IN_REJECT.getValue()
                );
            } else {
                // 调宿
                application.setApplicationStatus(
                        DEPARTMENT_CHANGE_DORM_REJECT.getValue()
                );
            }
            SEND_MAIL_THREAD_POOL.execute(() -> {
                sendNoticeMail(application, false);
            });
        }
        application.setUpdateTime(new Date(System.currentTimeMillis()));
        return updateById(application) ? R.ok() :
                R.error(ResponseCodeEnum.SERVER_ERROR, "数据库异常,更新申请状态失败");
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
                    "申请在本单位" +
                    (pass ? "已通过审核":"被驳回")+
                    "，请及时查看";
                sendMailUtil.sendMail(
                    email, subject, content
                );
            }
        }
    }
}
