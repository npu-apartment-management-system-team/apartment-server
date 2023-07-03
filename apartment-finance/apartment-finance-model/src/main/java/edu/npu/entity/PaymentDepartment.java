package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 代扣外部单位缴费表
 * @TableName payment_department
 */
@TableName(value ="payment_department")
@Data
@Builder
public class PaymentDepartment implements Serializable {
    /**
     * payment唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑外键。与department表中id字段是一致的。
     */
    private Long departmentId;

    /**
     * 本月/季度需缴纳金额
     */
    private Integer price;

    /**
     * 支付进展，分为0未支付 1已支付 default 0
     */
    private Integer hasPaid;

    /**
     * 铁路内部支票ID编号 可NULL
     */
    private String chequeId;

    /**
     * 完成支付的时间 可NULL
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date payTime;

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;
}
