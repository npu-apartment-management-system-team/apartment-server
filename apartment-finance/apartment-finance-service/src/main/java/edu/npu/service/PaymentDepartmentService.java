package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.PaymentDepartment;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 外部单位财务人员Service
* @createDate 2023-07-02 16:45:55
*/
public interface PaymentDepartmentService extends IService<PaymentDepartment> {

    /**
     * 查看历史变动表
     * @param accountUserDetails 获取登录用户所有权限
     * @param queryDto 分页查询Dto
     * @return R 历史变动表
     */
    R getVariationList(AccountUserDetails accountUserDetails, QueryDto queryDto);

    /**
     * 下载历史变动表
     * @param accountUserDetails 获取登录用户所有权限
     * @param downloadQueryDto 下载查询Dto
     * @return R 下载地址url
     */
    R downloadVariationList(AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto);

    /**
     * 查看外部单位代扣缴费情况
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @param queryDto 单位代扣缴费列表查询Dto
     * @return R 单位代扣缴费情况表
     */
    R getWithholdList(AccountUserDetails accountUserDetails ,QueryDto queryDto);

    /**
     * 查看某条代扣缴费具体情况
     *
     * @param id 该条代扣缴费id
     * @return R 该条代扣缴费具体情况
     */
    R getWithholdDetailById(Long id);

    /**
     * 下载外部单位代扣表
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @param downloadQueryDto 下载Dto
     * @return R 代扣表url
     */
    R downloadWithholdList(AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto);

    /**
     * 查看自收缴费情况
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @param queryDto 缴费列表查询Dto
     * @return R 自收缴费列表
     */
    R getChargeList(AccountUserDetails accountUserDetails, QueryDto queryDto);

    /**
     * 查看每条自收缴费具体情况
     *
     * @param id 缴费记录ID
     * @return R 该条自收缴费具体内容
     */
    R getChargeDetailById(Long id);

    /**
     * 下载自收表
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @param downloadQueryDto 下载Dto
     * @return R 自收表url
     */
    R downloadChargeList(AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto);

    /**
     * 外部单位填写必要信息以确认住宿费用代扣
     *
     * @param accountUserDetails 获取登录用户所有权限
     * @param id 外部单位缴费表id
     * @param chequeId 支票id
     * @return R 设置结果
     */
    R postChequeId(AccountUserDetails accountUserDetails, Long id, String chequeId);



}
