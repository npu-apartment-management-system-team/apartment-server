package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.entity.PaymentDepartment;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【payment_department(代扣外部单位缴费表)】的数据库操作Service
* @createDate 2023-07-02 16:45:55
*/
public interface PaymentDepartmentService extends IService<PaymentDepartment> {

    /**
     * 查看历史变动表
     * @param queryDto
     * @return R
     */
    R getVariationList(AccountUserDetails accountUserDetails, QueryDto queryDto);

    /**
     * 下载历史变动表
     * @param downloadQueryDto
     * @return R
     */
    R downloadVariationList(AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto);

    /**
     * 查看外部单位代扣缴费情况
     * @param queryDto
     * @return R
     */
    R getWithholdList(AccountUserDetails accountUserDetails ,QueryDto queryDto);

    /**
     * 查看某条代扣缴费具体情况
     * @param id
     * @return R
     */
    R getWithholdDetailById(Long id);

    /**
     * 下载外部单位代扣表
     * @param downloadQueryDto
     * @return R
     */
    R downloadWithholdList(AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto);

    /**
     * 查看自收缴费情况
     * @param queryDto
     * @return R
     */
    R getChargeList(AccountUserDetails accountUserDetails, QueryDto queryDto);

    /**
     * 查看每条自收缴费具体情况
     * @param id
     * @return R
     */
    R getChargeDetailById(Long id);

    /**
     * 下载自收表
     * @param downloadQueryDto
     * @return R
     */
    R downloadChargeList(AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto);

    /**
     * 外部单位填写必要信息以确认住宿费用代扣
     * @param id
     * @param checkId
     * @return R
     */
    R postChequeId(Long id, String checkId);



}
