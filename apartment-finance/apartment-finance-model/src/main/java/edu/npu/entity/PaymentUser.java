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
 * 自收与代扣自付部分缴费表
 * @TableName payment_user
 */
@TableName(value ="payment_user")
@Data
@Builder
public class PaymentUser implements Serializable {
    /**
     * payment唯一ID.支付宝发起支付的订单号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑外键。与user表中外键是一致的。
     */
    private Long userId;

    /**
     * 需缴纳金额
     */
    private Integer price;

    /**
     * 支付进展，分为0未支付 1支付中\_等待回调 2支付完成 default 0
     */
    private Integer status;

    /**
     * 缴费类别 0押金deposit 1住宿费 2网费
     */
    private Integer type;

    /**
     * 创建订单的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;

    /**
     * 当前订单状态更新的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date updateTime;

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;
}
