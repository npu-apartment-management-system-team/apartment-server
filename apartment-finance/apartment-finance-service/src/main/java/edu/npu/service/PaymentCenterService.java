package edu.npu.service;

import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.vo.R;

/**
 * @Author : Yu
 * @Date : 2023/7/3
 * @description : 房间公寓段财务人员service
 */
public interface PaymentCenterService {

    /**
     * 查看历史变动表
     *
     * @param queryDto 分页查询Dto
     * @return R 历史变动表
     */
    R getVariationList(QueryDto queryDto);

    /**
     * 下载历史变动表
     *
     * @param downloadQueryDto 下载查询Dto
     * @return R 下载地址url
     */
    R downloadVariationList(DownloadQueryDto downloadQueryDto);

    /**
     * 查看外部单位代扣缴费情况
     *
     * @param queryDto 外部单位代扣缴费列表查询Dto
     * @return R 单位代扣缴费情况表
     */
    R getWithholdList(QueryDto queryDto);

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
     * @param downloadQueryDto 下载Dto
     * @return R 代扣表url
     */
    R downloadWithholdList(DownloadQueryDto downloadQueryDto);

    /**
     * 查看自收缴费情况
     *
     * @param queryDto 缴费列表查询Dto
     * @return R 自收缴费列表
     */
    R getChargeList(QueryDto queryDto);

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
     * @param downloadQueryDto 下载Dto
     * @return R 自收表url
     */
    R downloadChargeList(DownloadQueryDto downloadQueryDto);

}
