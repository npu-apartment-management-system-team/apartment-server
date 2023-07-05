package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.npu.entity.Application;
import edu.npu.mapper.ApplicationMapper;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            //@Validated @SpringQueryMap UserPayListQueryDto userPayListQueryDto
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = true) Integer pageSize,
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
    @Nullable
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
}
