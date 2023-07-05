package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 住宿职工表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 住宿职工唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑外键，与login_account表中id字段构成一一对应关系。也是用户的手机号
     */
    @JsonAlias(value = "login_account_id")
    private Long loginAccountId;

    /**
     * 逻辑外键。用户所在外部单位ID。
     */
    @JsonAlias(value = "department_id")
    private Long departmentId;

    /**
     * 逻辑外键。与bed表对应的床ID。
     */
    @JsonAlias(value = "bed_id")
    private Long bedId;

    /**
     * 用户名称，应当与身份证上的人名一致
     */
    private String name;

    /**
     * 身份证号
     */
    @JsonAlias(value = "personal_id")
    private String personalId;

    /**
     * 身份证正面照片存储URL
     */
    @JsonAlias(value = "personal_card_url")
    private String personalCardUrl;

    /**
     * 人脸在阿里云人脸库中的ID
     */
    @JsonAlias(value = "face_id")
    private String faceId;

    /**
     * 职工人脸照片URL
     */
    @JsonAlias(value = "face_url")
    private String faceUrl;

    /**
     * 职工支付宝uuid
     */
    @JsonAlias(value = "alipay_id")
    private String alipayId;

    /**
     * 职工邮箱
     */
    private String email;

    /**
     * 性别 0男 1女
     */
    private Integer sex;

    /**
     * 是否处级干部 0非 1是
     */
    @JsonAlias(value = "is_cadre")
    private Integer isCadre;

    /**
     * 用户是否入住。 0未入住 1申请中 2已入住
     */
    private Integer status;

    /**
     * 缴费类型 0代扣 1自收
     */
    @JsonAlias(value = "pay_type")
    private Integer payType;

    /**
     * 是否需要缴纳网费 0非 1是
     */
    @JsonAlias(value = "network_enabled")
    private Integer networkEnabled;

    /**
     * 账号是否已删除 0未删除 1已删除
     */
    @TableLogic(value = "0", delval = "1")
    @JsonAlias(value = "is_deleted")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
