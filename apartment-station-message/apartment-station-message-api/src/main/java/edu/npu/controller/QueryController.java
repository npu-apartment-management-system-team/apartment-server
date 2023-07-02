package edu.npu.controller;

import edu.npu.dto.BasicQueryDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.QueryService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [查询控制器]
 */
@RestController
public class QueryController {

    @Resource
    private QueryService queryService;

    @GetMapping("/sender/outbox")
    public R querySenderOutbox(
            @AuthenticationPrincipal AccountUserDetails accountUserDetails,
            @Validated BasicQueryDto queryDto){
        return queryService.querySenderOutbox(
                accountUserDetails, queryDto);
    }

    @GetMapping("/receiver/inbox")
    public R queryReceiverInbox(
            @AuthenticationPrincipal AccountUserDetails accountUserDetails,
            @Validated BasicQueryDto queryDto){
        return queryService.queryReceiverInbox(
                accountUserDetails, queryDto);
    }
}
