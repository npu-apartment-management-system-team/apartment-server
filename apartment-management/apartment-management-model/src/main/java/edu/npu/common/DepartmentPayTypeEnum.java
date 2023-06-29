package edu.npu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum DepartmentPayTypeEnum {

    @JsonProperty("MONTHLY")
    MONTHLY(0),

    @JsonProperty("QUARTERLY")
    QUARTERLY(1);

    private final int value;

    DepartmentPayTypeEnum(int value) {
        this.value = value;
    }

    private static DepartmentPayTypeEnum fromValue(int value) {
            for (DepartmentPayTypeEnum e : DepartmentPayTypeEnum.values()) {
                if (value == e.getValue()) {
                    return e;
                }
            }
            return null;
    }
}
