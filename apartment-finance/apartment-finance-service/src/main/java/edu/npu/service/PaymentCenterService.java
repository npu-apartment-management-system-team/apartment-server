package edu.npu.service;

import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.vo.R;

/**
 * @Author: Yu
 * @Date: 2023/7/3
 */
public interface PaymentCenterService {

    /**
     * 查看历史变动表
     * @param queryDto
     * @return
     */
    R getVariationList(QueryDto queryDto);

    /**
     * 下载历史变动表
     * @param downloadQueryDto
     * @return
     */
    R downloadVariationList(DownloadQueryDto downloadQueryDto);

    /**
     * 查看外部单位代扣缴费情况
     * @param queryDto
     * @return
     */
    R getWithholdList(QueryDto queryDto);

    /**
     * 查看某条代扣缴费具体情况
     * @param id
     * @return
     */
    R getWithholdDetailById(Long id);

    /**
     * 下载外部单位代扣表
     * @param downloadQueryDto
     * @return
     */
    R downloadWithholdList(DownloadQueryDto downloadQueryDto);

    /**
     * 查看自收缴费情况
     * @param queryDto
     * @return
     */
    R getChargeList(QueryDto queryDto);

    /**
     * 查看每条自收缴费具体情况
     * @param id
     * @return
     */
    R getChargeDetailById(Long id);

    /**
     * 下载自收表
     * @param downloadQueryDto
     * @return
     */
    R downloadChargeList(DownloadQueryDto downloadQueryDto);

}
