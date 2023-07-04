package edu.npu.controller;

import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.PaymentDepartmentService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : [wangminan]
 * @description : [外部单位财务管理控制器类]
 */
@RestController
@RequestMapping("/department")
@Slf4j
public class DepartmentController {

    @Resource
    private PaymentDepartmentService paymentDepartmentService;

    @InitBinder  //解决前段传过来时间的字符串解析成时间报错问题
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"),true));
    }

    @GetMapping("/variation")
    public R getVariationList(@AuthenticationPrincipal AccountUserDetails accountUserDetails, @Validated QueryDto queryDto)  {
        return paymentDepartmentService.getVariationList(accountUserDetails, queryDto);
    }


    @GetMapping("/variation/download")
    R downloadVariationList(@AuthenticationPrincipal AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto) {
        return paymentDepartmentService.downloadVariationList(accountUserDetails, downloadQueryDto);
    }


    @GetMapping("/withhold")
    R getWithholdList(@AuthenticationPrincipal AccountUserDetails accountUserDetails, QueryDto queryDto) {
        return paymentDepartmentService.getWithholdList(accountUserDetails, queryDto);
    }


    @GetMapping("/withhold/detail")
    R getWithholdDetailById(Long id) {
        return paymentDepartmentService.getWithholdDetailById(id);
    }


    @GetMapping("/withhold/download")
    R downloadWithholdList(@AuthenticationPrincipal AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto) {
        return paymentDepartmentService.downloadWithholdList(accountUserDetails, downloadQueryDto);
    }


    @GetMapping("/charge")
    R getChargeList(@AuthenticationPrincipal AccountUserDetails accountUserDetails, QueryDto queryDto) {
        return paymentDepartmentService.getChargeList(accountUserDetails, queryDto);
    }


    @GetMapping("/charge/detail")
    R getChargeDetailById(Long id) {
        return paymentDepartmentService.getChargeDetailById(id);
    }


    @GetMapping("/charge/download")
    R downloadChargeList(@AuthenticationPrincipal AccountUserDetails accountUserDetails, DownloadQueryDto downloadQueryDto) {
        return paymentDepartmentService.downloadChargeList(accountUserDetails, downloadQueryDto);
    }
}
