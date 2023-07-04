package edu.npu.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * @Author: Yu
 * @Date: 2023/7/3
 */
@Data
@Builder
public class Application implements Serializable {

    /**
     * application唯一ID
     */
    private Long id;

    /**
     * 逻辑外键，与user表的id构成映射关系
     */
    private Long userId;

    /**
     * 逻辑外键，押金订单号，与payment表的id构成映射关系。
     */
    private Long paymentId;

    /**
     * 申请类型。包括0入住、1调宿、2退宿
     */
    private int type;

    /**
     * 申请文件在OSS的存档URL。不可删除，冷备。
     */
    private String fileUrl;

    /**
     * 申请进展，应该是一个两段式的结构。eg.1_1 入住本单位审批中 具体见常量类 考虑撤回
     */
    private int applicationStatus;

    /**
     * 押金缴纳状态 0未缴纳 1已缴纳 2已退回 结合payment_id default 0
     */
    private int depositStatus;

    /**
     * 创建申请的时间 yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 当前申请状态更新的时间 yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

}
