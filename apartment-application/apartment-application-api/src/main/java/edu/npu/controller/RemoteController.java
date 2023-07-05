package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.npu.common.ApplicationStatusEnum;
import edu.npu.entity.Application;
import edu.npu.mapper.ApplicationMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author : [wangminan]
 * @description : [Application模块用于远程调用的Controller]
 */
@RestController
@RequestMapping("/remote")
public class RemoteController {

    @Resource
    private ApplicationMapper applicationMapper;

    @GetMapping("/query/page")
    public Page<Application> getApplicationPageForQuery(
            @RequestParam(value = "pageNum") Integer pageNum,
            @RequestParam(value = "pageSize") Integer pageSize,
            @RequestParam(value = "beginTime", required = false) Date beginTime,
            @RequestParam(value = "departmentId", required = false) Long departmentId
    ) {
        Page<Application> page = new Page<>(
                pageNum, pageSize);
       return applicationMapper.selectPage(page, getApplicationLambdaQueryWrapper(
                beginTime, departmentId));
    }

    @GetMapping("/download/list")
    public List<Application> getApplicationListForDownload(
            @RequestParam(value = "beginTime", required = false) Date beginTime,
            @RequestParam(value = "departmentId", required = false) Long departmentId
    ) {
        return applicationMapper.selectList(getApplicationLambdaQueryWrapper(beginTime, departmentId));
    }

    /**
     * 设置wrapper
     * @param beginTime 开始时间
     * @param departmentId 部门ID
     * @return R
     */
    private LambdaQueryWrapper<Application> getApplicationLambdaQueryWrapper(
            Date beginTime, Long departmentId) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();

        if(beginTime != null) {
            wrapper.ge(Application::getCreateTime, beginTime);
        }

        if(departmentId != null) {
            wrapper.inSql(Application::getUserId, "SELECT id FROM user WHERE department_id = '"+ departmentId +"'");
        }

        wrapper.in(Application::getApplicationStatus, "10", "20", "30");
        wrapper.orderByDesc(Application::getCreateTime);
        return wrapper;
    }

    @PutMapping("/deposit/{userId}")
    public boolean updateDepositApplicationByUserId(
            @PathVariable(value = "userId") Long userId) {
        Application application =  applicationMapper.selectOne(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getUserId, userId)
                        .eq(Application::getApplicationStatus,
                                ApplicationStatusEnum.CENTER_DORM_ALLOCATION.getValue()));
        application.setApplicationStatus(
                ApplicationStatusEnum.CHECK_IN_DEPOSIT.getValue()
        );
        return applicationMapper.updateById(application) > 0;
    }
}
