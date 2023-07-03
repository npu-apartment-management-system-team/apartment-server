package edu.npu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.UserPayListQueryDto;
import edu.npu.entity.Application;
import edu.npu.service.PaymentCenterService;
import edu.npu.vo.R;

/**
 * @Author: Yu
 * @Date: 2023/7/3
 */
public class PaymentCenterServiceImpl implements PaymentCenterService {

    /**
     * 查看历史变动表
     *
     * @param userPayListQueryDto
     * @return
     */
    @Override
    public R getVariationList(UserPayListQueryDto userPayListQueryDto) {

        IPage<Application> page = new Page<>(
                userPayListQueryDto.pageNum(), userPayListQueryDto.pageSize());

        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        boolean hasQuery = false;

        if(userPayListQueryDto.beginTime() != null) {
            hasQuery = true;
            wrapper.ge(Application::getCreate_time, userPayListQueryDto.beginTime());
        }

        if(userPayListQueryDto.departmentId() != null) {
            hasQuery = true;
            wrapper.inSql(Application::getUser_id, "select id from user where department_id = ${queryDto.departmentId()}");
        }

        if (hasQuery) {
            //page = (page, wrapper);
        } else {
            //page = page(page, null);
        }

        return null;
    }

    /**
     * 下载历史变动表
     *
     * @param downloadQueryDto
     * @return
     */
    @Override
    public R downloadVariationList(DownloadQueryDto downloadQueryDto) {
        return null;
    }

    /**
     * 查看外部单位代扣缴费情况
     *
     * @param userPayListQueryDto
     * @return
     */
    @Override
    public R getWithholdList(UserPayListQueryDto userPayListQueryDto) {
        return null;
    }

    /**
     * 查看某条代扣缴费具体情况
     *
     * @param id
     * @return
     */
    @Override
    public R getWithholdDetailById(Long id) {
        return null;
    }

    /**
     * 下载外部单位代扣表
     *
     * @param downloadQueryDto
     * @return
     */
    @Override
    public R downloadWithholdList(DownloadQueryDto downloadQueryDto) {
        return null;
    }

    /**
     * 查看自收缴费情况
     *
     * @param userPayListQueryDto
     * @return
     */
    @Override
    public R getChargeList(UserPayListQueryDto userPayListQueryDto) {
        return null;
    }

    /**
     * 查看每条自收缴费具体情况
     *
     * @param id
     * @return
     */
    @Override
    public R getChargeDetailById(Long id) {
        return null;
    }

    /**
     * 下载自收表
     *
     * @param downloadQueryDto
     * @return
     */
    @Override
    public R downloadChargeList(DownloadQueryDto downloadQueryDto) {
        return null;
    }
}
