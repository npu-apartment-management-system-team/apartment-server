package edu.npu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum UserStatusEnum {
    // 未入住0 申请中1 已入住2
    @JsonProperty("NOT_CHECK_IN")
    NOT_CHECK_IN(0),

    @JsonProperty("CHECK_IN_APPLICATION")
    CHECK_IN_APPLICATION(1),

    @JsonProperty("CHECK_IN")
    CHECK_IN(2);

    private final int value;

    UserStatusEnum(int value) {
        this.value = value;
    }

    public static UserStatusEnum getUserStatusEnumByValue(int value) {
        for (UserStatusEnum userStatusEnum : UserStatusEnum.values()) {
            if (userStatusEnum.value == value) {
                return userStatusEnum;
            }
        }
        return null;
    }
}
