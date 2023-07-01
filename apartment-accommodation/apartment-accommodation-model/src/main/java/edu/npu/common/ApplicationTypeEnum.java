package edu.npu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author : [wangminan]
 * @description : [申请类型枚举类]
 */
@Getter
public enum ApplicationTypeEnum {

    // 入住申请
    @JsonProperty("CHECK_IN")
    CHECK_IN(0),

    // 调宿
    @JsonProperty("CHANGE_DORM")
    CHANGE_DORM(1),

    // 退宿
    @JsonProperty("CHECK_OUT")
    CHECK_OUT(2);

    private final int value;

    ApplicationTypeEnum(int value) {
        this.value = value;
    }

    public static ApplicationTypeEnum fromValue(int value) {
        for (ApplicationTypeEnum applicationTypeEnum : ApplicationTypeEnum.values()) {
            if (applicationTypeEnum.value == value) {
                return applicationTypeEnum;
            }
        }
        return null;
    }
}
