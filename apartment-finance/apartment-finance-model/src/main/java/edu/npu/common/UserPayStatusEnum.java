package edu.npu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.checkerframework.common.reflection.qual.GetClass;

/**
 * @author : [wangminan]
 * @description : [用户支付状态枚举类]
 */
@Getter
public enum UserPayStatusEnum {
    // 0未支付 1支付中_等待回调 2支付完成 default 0

    @JsonProperty("UNPAID")
    UNPAID(0),

    @JsonProperty("PAYING")
    PAYING(1),

    @JsonProperty("PAID")
    PAID(2);

    private final int value;

    UserPayStatusEnum(int value) {
        this.value = value;
    }

    public UserPayStatusEnum fromValue(int value) {
        for (UserPayStatusEnum statusEnum : UserPayStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}
