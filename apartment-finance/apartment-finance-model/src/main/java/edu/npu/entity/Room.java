package edu.npu.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 房间表
 * @TableName room
 */
@Data
public class Room implements Serializable {
    /**
     * room唯一ID
     */
    private Long id;

    /**
     * 逻辑外键。对应apartment表中的id字段。
     */
    private Long apartmentId;

    /**
     * 房间名称。eg.1-2-101
     */
    private String name;

    /**
     * 房间作用。eg. 住宿 班组长办公室 活动室 空房间...
     */
    private String usage;

    /**
     * 是否是处级干部房 0非 1是 default 0
     */
    private Integer isForCadre;

    /**
     * 是否属于预留空房间 0非 1是 default 0
     */
    private Integer isReserved;

    /**
     * 性别 0男 1女 我想了想还是int吧 用boolean真不合适
     */
    private Integer sex;

    /**
     * 房间类型。1单人间 2双人间 3三人间 4四人间(极少) default2
     */
    private Integer type;

    /**
     * 房间总价。
     */
    private Integer totalFee;

    /**
     * 自理部分。仅代扣用户会用到该字段。
     */
    private Integer selfPayFee;

    /**
     * 单位报销部分。仅代扣用户会用到该字段。
     */
    private Integer refundFee;

    @Serial
    private static final long serialVersionUID = 1L;
}
