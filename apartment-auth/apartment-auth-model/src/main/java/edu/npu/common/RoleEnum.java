package edu.npu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author wangminan
 * @description 用户角色枚举 一共有8种角色
 */
@Getter
public enum RoleEnum {

    // 超级管理员
    @JsonProperty("SUPER_ADMIN")
    SUPER_ADMIN(1),

    // 房建公寓段入住/调宿办理员
    @JsonProperty("CENTER_CHECK_IN_CLERK")
    CENTER_CHECK_IN_CLERK(2),

    // 房建公寓段宿舍分配员
    @JsonProperty("CENTER_DORM_ALLOCATION_CLERK")
    CENTER_DORM_ALLOCATION_CLERK(3),

    // 房建公寓段财务人员
    @JsonProperty("CENTER_FINANCE_CLERK")
    CENTER_FINANCE_CLERK(4),

    // 房建公寓段宿管班组
    @JsonProperty("CENTER_DORM_MANAGER")
    CENTER_DORM_MANAGER(5),

    // 外部单位入住办理人员
    @JsonProperty("DEPARTMENT_CHECK_IN_CLERK")
    DEPARTMENT_CHECK_IN_CLERK(6),

    // 外部单位财务人员
    @JsonProperty("DEPARTMENT_FINANCE_CLERK")
    DEPARTMENT_FINANCE_CLERK(7),

    // 住宿职工
    @JsonProperty("USER")
    USER(8);

    RoleEnum(int value) {
        this.value = value;
    }

    public static RoleEnum fromValue(int value) {

        for (RoleEnum e : RoleEnum.values()) {
            if (value == e.getValue()) {
                return e;
            }
        }
        return null;
    }

    private final int value;

}

