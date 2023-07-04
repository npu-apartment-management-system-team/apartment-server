package edu.npu.controller;

import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.service.PaymentCenterService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    @InitBinder  //解决前段传过来时间的字符串解析成时间报错问题
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"),true));
    }

    @GetMapping("/variation")
    public R getVariationList(@Validated QueryDto queryDto)  {
        return paymentCenterService.getVariationList(queryDto);
    }


    @GetMapping("/variation/download")
    R downloadVariationList(DownloadQueryDto downloadQueryDto) {
        return paymentCenterService.downloadVariationList(downloadQueryDto);
    }


    @GetMapping("/withhold")
    R getWithholdList(QueryDto queryDto) {
        return paymentCenterService.getWithholdList(queryDto);
    }


    @GetMapping("/withhold/detail")
    R getWithholdDetailById(Long id) {
        return paymentCenterService.getWithholdDetailById(id);
    }


    @GetMapping("/withhold/download")
    R downloadWithholdList(DownloadQueryDto downloadQueryDto) {
        return paymentCenterService.downloadWithholdList(downloadQueryDto);
    }


    @GetMapping("/charge")
    R getChargeList(QueryDto queryDto) {
        return paymentCenterService.getChargeList(queryDto);
    }


    @GetMapping("/charge/detail")
    R getChargeDetailById(Long id) {
        return paymentCenterService.getChargeDetailById(id);
    }


    @GetMapping("/charge/download")
    R downloadChargeList(DownloadQueryDto downloadQueryDto) {
        return paymentCenterService.downloadChargeList(downloadQueryDto);
    }
}
