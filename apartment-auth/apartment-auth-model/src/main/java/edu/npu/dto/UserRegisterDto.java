package edu.npu.dto;

import edu.npu.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * @author : [wangminan]
 * @description : [用户注册的数据传输对象]
 */
public record UserRegisterDto(
        @NotNull
        @Pattern(regexp = RegexPatterns.PHONE_REGEX, message = "手机号格式不正确")
        String username,
        // 非空即可 密码经过RSA加密
        @NotNull
        String password,
        String email,
        Long departmentId,
        @NotNull
        String name,
        @Pattern(regexp = RegexPatterns.ID_CARD_REGEX, message = "身份证格式不正确")
        String personalId,
        // 身份证OSS URL
        @NotNull
        String personalCardUrl,
        // 人脸OSS URL
        @NotNull
        String faceUrl,
        Integer sex,
        Boolean isCadre
) {

}
