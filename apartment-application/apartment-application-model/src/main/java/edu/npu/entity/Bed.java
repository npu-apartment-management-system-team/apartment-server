package edu.npu.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 床位表
 * @TableName bed
 */
@Data
public class Bed implements Serializable {
    /**
     * bed唯一ID
     */
    private Long id;

    /**
     * 逻辑外键。对应room表中的id字段。
     */
    private Long roomId;

    /**
     * 床位名称 用于承接既有数据 有ABCD 1234号等多种命名方式
     */
    private String name;

    /**
     * 逻辑外键 职工缴纳押金后payment_user表中的订单号
     */
    private Long receiptId;

    /**
     * 是否正被占用。 0 空床 1已被占用 default0
     */
    private Integer isInUse;

    /**
     * 是否已删除 0未删除 1已删除 default 0
     */
    private Integer isDeleted;

    @Serial
    private static final long serialVersionUID = 1L;
}
