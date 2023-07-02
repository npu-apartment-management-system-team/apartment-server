package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 申请表
 * @TableName application
 */
@TableName(value ="application")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application implements Serializable {
    /**
     * application唯一ID
     */
    @TableId(type = IdType.AUTO)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 逻辑外键，与user表的id构成映射关系
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    /**
     * 逻辑外键，押金订单号，与payment表的id构成映射关系。
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long paymentId;

    /**
     * 申请类型。包括0入住、1调宿、2退宿
     */
    private Integer type;

    /**
     * 申请文件在OSS的存档URL。不可删除，冷备。
     */
    private String fileUrl;

    /**
     * 申请进展，应该是一个两段式的结构。eg.0_1 入住本单位审批中 具体见常量类 考虑撤回
     */
    private Integer applicationStatus;

    /**
     * 押金缴纳状态 0未缴纳 1已缴纳 2已退回 结合payment_id default 0
     */
    private Integer depositStatus;

    /**
     * 创建申请的时间 yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 当前申请状态更新的时间 yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;
}
