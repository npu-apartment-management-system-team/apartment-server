package edu.npu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Getter
public enum BedInUseEnum {

    @JsonProperty("NOT_IN_USE")
    NOT_IN_USE(0),

    @JsonProperty("IN_USE")
    IN_USE(1);

    private final int value;

    BedInUseEnum(int value) {
        this.value = value;
    }

    public static BedInUseEnum getBedInUseEnumByValue(int value) {
        for (BedInUseEnum bedInUseEnum : BedInUseEnum.values()) {
            if (bedInUseEnum.value == value) {
                return bedInUseEnum;
            }
        }
        return null;
    }
}
