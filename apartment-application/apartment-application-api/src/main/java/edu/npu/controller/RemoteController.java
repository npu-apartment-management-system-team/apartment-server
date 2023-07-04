package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.Application;
import edu.npu.mapper.ApplicationMapper;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
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
            @Validated UserPayListQueryDto userPayListQueryDto,
            Long departmentId
    ) {
        Page<Application> page = new Page<>(
                userPayListQueryDto.pageNum(), userPayListQueryDto.pageSize());
       return applicationMapper.selectPage(page, getApplicationLambdaQueryWrapper(
                userPayListQueryDto.beginTime(), departmentId));
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
        boolean hasQuery = false;

        if(beginTime != null) {
            hasQuery = true;
            wrapper.ge(Application::getCreateTime, beginTime);
        }

        if(departmentId != null) {
            hasQuery = true;
            wrapper.inSql(Application::getUserId, "select id from user where department_id = ${queryDto.departmentId()}");
        }

        if(!hasQuery) {
            return null;
        }

        wrapper.orderByDesc(Application::getCreateTime);
        return wrapper;
    }
}
