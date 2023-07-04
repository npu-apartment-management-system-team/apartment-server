package edu.npu.controller;



import edu.npu.entity.AccountUserDetails;
import edu.npu.service.UserLivingMessageService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user/living")
public class UserLivingMessageController {

    @Resource
    private UserLivingMessageService userLivingMessageService;

    @GetMapping("/message")
    @PreAuthorize("hasAuthority('USER')")
    public R getUserLivingMessage(@AuthenticationPrincipal AccountUserDetails accountUserDetails) {
        return userLivingMessageService.getUserLivingMessage(accountUserDetails);
    }
}
