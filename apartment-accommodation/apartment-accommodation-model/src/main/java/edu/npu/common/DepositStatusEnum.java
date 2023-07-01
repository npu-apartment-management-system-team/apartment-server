package edu.npu.common;

import lombok.Getter;

/**
 * @author : [wangminan]
 * @description : [押金状态枚举类]
 */
@Getter
public enum DepositStatusEnum {
    // 0未缴纳
    NOT_PAID(0),

    // 1已缴纳
    PAID(1),

    // 2已退回
    RETURNED(2);

    private final int value;

    DepositStatusEnum(int value) {
        this.value = value;
    }

    public static DepositStatusEnum fromValue(int value) {
        for (DepositStatusEnum depositStatusEnum : DepositStatusEnum.values()) {
            if (depositStatusEnum.value == value) {
                return depositStatusEnum;
            }
        }
        return null;
    }
}
