package edu.npu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author wangminan
 * @description 申请状态枚举类
 */
@Getter
public enum ApplicationStatusEnum {

    // 入住申请流程完成
    @JsonProperty("CHECK_IN_COMPLETE")
    CHECK_IN_COMPLETE(1_0),

    // 提交入住申请
    @JsonProperty("CHECK_IN_SUBMIT")
    CHECK_IN_SUBMIT(1_1),

    // 本单位入住审批
    @JsonProperty("DEPARTMENT_CHECK_IN_APPROVAL")
    DEPARTMENT_CHECK_IN_APPROVAL(1_2),

    // 房寓段入住审批
    @JsonProperty("CENTER_CHECK_IN_APPROVAL")
    CENTER_CHECK_IN_APPROVAL(1_3),

    // 房建段入住分配宿舍
    @JsonProperty("CENTER_DORM_ALLOCATION")
    CENTER_DORM_ALLOCATION(1_4),

    // 入住待缴纳押金
    @JsonProperty("CHECK_IN_DEPOSIT")
    CHECK_IN_DEPOSIT(1_5),

    // 入住申请被本单位驳回
    @JsonProperty("DEPARTMENT_CHECK_IN_REJECT")
    DEPARTMENT_CHECK_IN_REJECT(1_6),

    // 入住申请被房建段驳回
    @JsonProperty("CENTER_CHECK_IN_REJECT")
    CENTER_CHECK_IN_REJECT(1_7),

    // 缴纳押金超时
    @JsonProperty("CHECK_IN_DEPOSIT_TIMEOUT")
    CHECK_IN_DEPOSIT_TIMEOUT(1_8),

    // 班组长确认超时
    @JsonProperty("CENTER_DORM_MANAGER_CHECK_IN_CONFIRM_TIMEOUT")
    CENTER_DORM_MANAGER_CHECK_IN_CONFIRM_TIMEOUT(1_9),

    // 用户撤回入住申请
    @JsonProperty("CHECK_IN_WITHDRAW")
    CHECK_IN_WITHDRAW(1_10),

    // 调宿申请完成
    @JsonProperty("DEPARTMENT_CHANGE_DORM_COMPLETE")
    CHANGE_DORM_COMPLETE(2_0),

    // 提交调宿申请
    @JsonProperty("CHANGE_DORM_SUBMIT")
    CHANGE_DORM_SUBMIT(2_1),

    // 本单位通过调宿申请
    @JsonProperty("DEPARTMENT_CHANGE_DORM_APPROVAL")
    DEPARTMENT_CHANGE_DORM_APPROVAL(2_2),

    // 房建段通过调宿申请
    @JsonProperty("CENTER_CHANGE_DORM_APPROVAL")
    CENTER_CHANGE_DORM_APPROVAL(2_3),

    // 房建段分配新宿舍
    @JsonProperty("CENTER_DORM_ALLOCATION")
    CENTER_DORM_CHANGE_ALLOCATION(2_4),

    // 原有宿管确认离宿
    @JsonProperty("CENTER_DORM_MANAGER_CHECK_OUT_CONFIRM")
    CENTER_DORM_MANAGER_CHANGE_CHECK_OUT_CONFIRM(2_5),

    // 调宿申请被本单位驳回
    @JsonProperty("DEPARTMENT_CHANGE_DORM_REJECT")
    DEPARTMENT_CHANGE_DORM_REJECT(2_6),

    // 调宿申请被房建段驳回
    @JsonProperty("CENTER_CHANGE_DORM_REJECT")
    CENTER_CHANGE_DORM_REJECT(2_7),

    // 原有宿管确认离宿超时
    @JsonProperty("CENTER_DORM_MANAGER_CHECK_OUT_CONFIRM_TIMEOUT")
    CENTER_DORM_MANAGER_CHANGE_CHECK_OUT_CONFIRM_TIMEOUT(2_8),

    // 新宿管确认入住超时
    @JsonProperty("CENTER_DORM_MANAGER_CHECK_IN_CONFIRM_TIMEOUT")
    CENTER_DORM_MANAGER_CHANGE_CHECK_IN_CONFIRM_TIMEOUT(2_9),

    // 用户撤回调宿申请
    @JsonProperty("CHANGE_DORM_WITHDRAW")
    CHANGE_DORM_WITHDRAW(2_10),

    // 退宿流程完成
    @JsonProperty("CHECK_OUT_COMPLETE")
    CHECK_OUT_COMPLETE(3_0),

    // 提交退宿申请
    @JsonProperty("CHECK_OUT_SUBMIT")
    CHECK_OUT_SUBMIT(3_1),

    // 用户撤回退宿申请
    @JsonProperty("CHECK_OUT_WITHDRAW")
    CHECK_OUT_WITHDRAW(3_3);

    private final int value;


    ApplicationStatusEnum(int value) {
        this.value = value;
    }

    public static ApplicationStatusEnum fromValue(int value) {
        for (ApplicationStatusEnum status : ApplicationStatusEnum.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }
}
