package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.dto.AddFaceDto;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.User;
import edu.npu.service.FaceService;
import edu.npu.service.LoginAccountService;
import edu.npu.service.UserService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author : [wangminan]
 * @description : [远程调用人脸服务]
 */
@RestController
@RequestMapping("/face")
public class FaceController {

    @Resource
    private FaceService faceService;

    @Resource
    private UserService userService;

    @Resource
    private LoginAccountService loginAccountService;

    @GetMapping("/user")
    public R getUserByFace(
            @RequestParam(value = "faceUrl") String faceUrl) {
        String entityId = faceService.getEntityIdByFace(faceUrl);
        LoginAccount loginAccount = loginAccountService.getOne(
                new LambdaQueryWrapper<LoginAccount>()
                        .eq(LoginAccount::getUsername, entityId)
        );
        if (loginAccount == null) {
            return R.error(ResponseCodeEnum.NOT_FOUND, "未找到用户");
        }
        User user = userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getLoginAccountId, loginAccount.getId())
        );
        return R.ok().put("user", user);
    }

    @GetMapping("/entityId")
    public String getEntityIdByFace(
            @RequestParam(value = "faceUrl") String faceUrl) {
        return faceService.getEntityIdByFace(faceUrl);
    }

    @PostMapping
    public String addFace(@Validated @RequestBody AddFaceDto addFaceDto) {
        return faceService.addFace(addFaceDto.entityId(), addFaceDto.faceUrl());
    }

    @DeleteMapping("/entity/{entityId}")
    public boolean deleteFaceEntity(
            @PathVariable(value = "entityId") String entityId) {
        return faceService.deleteFaceEntity(entityId);
    }

    @DeleteMapping("/{faceId}")
    public boolean deleteFace(
            @PathVariable(value = "faceId") String faceId) {
        return faceService.deleteFace(faceId);
    }
}
