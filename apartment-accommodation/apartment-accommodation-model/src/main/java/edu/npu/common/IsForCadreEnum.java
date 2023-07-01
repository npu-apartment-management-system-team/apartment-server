package edu.npu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum IsForCadreEnum {

    @JsonProperty("NOT_FOR_CADRE")
    NOT_FOR_CADRE(0),

    @JsonProperty("IS_FOR_CADRE")
    IS_FOR_CADRE(1);

    private final int value;

    IsForCadreEnum(int value) {
        this.value = value;
    }

    public static IsForCadreEnum getRoomIsForCadreEnumByValue(int value) {
        for (IsForCadreEnum isForCadreEnum : IsForCadreEnum.values()) {
            if (isForCadreEnum.value == value) {
                return isForCadreEnum;
            }
        }
        return null;
    }
}
