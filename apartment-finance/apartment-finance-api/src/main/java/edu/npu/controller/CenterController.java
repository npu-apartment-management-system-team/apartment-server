package edu.npu.controller;

import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.service.PaymentCenterService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [房间公寓段财务管理控制器类]
 */
@RestController
@RequestMapping("/center")
@Slf4j
public class CenterController {

    @Resource
    private PaymentCenterService paymentCenterService;

    @GetMapping("/variation")
    public R getVariationList(@Validated QueryDto queryDto)  {
        return paymentCenterService.getVariationList(queryDto);
    }


    @GetMapping("/variation/download")
    public R downloadVariationList(DownloadQueryDto downloadQueryDto) {
        return paymentCenterService.downloadVariationList(downloadQueryDto);
    }


    @GetMapping("/withhold")
    public R getWithholdList(QueryDto queryDto) {
        return paymentCenterService.getWithholdList(queryDto);
    }


    @GetMapping("/withhold/detail")
    public R getWithholdDetailById(Long id) {
        return paymentCenterService.getWithholdDetailById(id);
    }


    @GetMapping("/withhold/download")
    public R downloadWithholdList(DownloadQueryDto downloadQueryDto) {
        return paymentCenterService.downloadWithholdList(downloadQueryDto);
    }


    @GetMapping("/charge")
    @PreAuthorize("hasAuthority('CENTER_FINANCE_CLERK')")
    public R getChargeList(QueryDto queryDto) {
        return paymentCenterService.getChargeList(queryDto);
    }


    @GetMapping("/charge/detail")
    @PreAuthorize("hasAuthority('CENTER_FINANCE_CLERK')")
    public R getChargeDetailById(Long id) {
        return paymentCenterService.getChargeDetailById(id);
    }


    @GetMapping("/charge/download")
    @PreAuthorize("hasAuthority('CENTER_FINANCE_CLERK')")
    public R downloadChargeList(DownloadQueryDto downloadQueryDto) {
        return paymentCenterService.downloadChargeList(downloadQueryDto);
    }
}
