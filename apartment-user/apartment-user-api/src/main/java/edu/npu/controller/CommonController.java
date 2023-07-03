package edu.npu.controller;

import edu.npu.dto.UpdatePasswordDto;
import edu.npu.entity.AccountUserDetails;
import edu.npu.service.CommonService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author : [wangminan]
 * @description : [通用控制器类]
 */
@Controller
@RequestMapping("/password")
public class CommonController {
    @Resource
    private CommonService commonService;

    @PutMapping
    public R UpdatePassword(
            @AuthenticationPrincipal AccountUserDetails accountUserDetails,
            @RequestBody UpdatePasswordDto updatePasswordDto){
        Long id = accountUserDetails.getId();
        return commonService.updatePassword(id,updatePasswordDto);
    }
}
