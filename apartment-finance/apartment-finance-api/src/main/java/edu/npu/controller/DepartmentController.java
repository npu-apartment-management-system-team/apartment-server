package edu.npu.controller;

import edu.npu.dto.DownloadQueryDto;
import edu.npu.dto.QueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.PaymentDepartmentService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/variation")
    public R getVariationList(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                              @Validated QueryDto queryDto)  {
        return paymentDepartmentService.getVariationList(accountUserDetails, queryDto);
    }


    @GetMapping("/variation/download")
    public R downloadVariationList(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                            DownloadQueryDto downloadQueryDto) {
        return paymentDepartmentService.downloadVariationList(accountUserDetails, downloadQueryDto);
    }


    @GetMapping("/withhold")
    public R getWithholdList(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                      QueryDto queryDto) {
        return paymentDepartmentService.getWithholdList(accountUserDetails, queryDto);
    }


    @GetMapping("/withhold/detail")
    public R getWithholdDetailById(Long id) {
        return paymentDepartmentService.getWithholdDetailById(id);
    }


    @GetMapping("/withhold/download")
    public R downloadWithholdList(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                           DownloadQueryDto downloadQueryDto) {
        return paymentDepartmentService.downloadWithholdList(accountUserDetails, downloadQueryDto);
    }


    @GetMapping("/charge")
    public R getChargeList(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                    QueryDto queryDto) {
        return paymentDepartmentService.getChargeList(accountUserDetails, queryDto);
    }


    @GetMapping("/charge/detail")
    public R getChargeDetailById(Long id) {
        return paymentDepartmentService.getChargeDetailById(id);
    }


    @GetMapping("/charge/download")
    public R downloadChargeList(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                         DownloadQueryDto downloadQueryDto) {
        return paymentDepartmentService.downloadChargeList(accountUserDetails, downloadQueryDto);
    }

    @PostMapping("/pay/{id}")
    public R postChequeId(@AuthenticationPrincipal AccountUserDetails accountUserDetails,
                   @PathVariable("id") Long id, @RequestParam String chequeId) {
        return paymentDepartmentService.postChequeId(accountUserDetails, id, chequeId);
    }
}
