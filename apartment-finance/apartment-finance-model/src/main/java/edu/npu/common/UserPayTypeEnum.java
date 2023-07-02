package edu.npu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author : [wangminan]
 * @description : [用户支付类型枚举类]
 */
@Getter
public enum UserPayTypeEnum {

    // 0押金deposit 1住宿费 2网费
    @JsonProperty("DEPOSIT")
    DEPOSIT(0),

    @JsonProperty("ACCOMMODATION")
    ACCOMMODATION(1),

    @JsonProperty("NETWORK")
    NETWORK(2);

    private final int value;

    UserPayTypeEnum(int value) {
        this.value = value;
    }

    public static UserPayTypeEnum valueOf(int value) {
        for (UserPayTypeEnum typeEnum : UserPayTypeEnum.values()) {
            if (typeEnum.value == value) {
                return typeEnum;
            }
        }
        return null;
    }
}
