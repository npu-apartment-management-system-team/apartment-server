package edu.npu.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员
 * @TableName admin
 */
@Data
public class Admin implements Serializable {
    /**
     * 管理员唯一ID
     */
    private Long id;

    /**
     * 逻辑外键，与login_account表中id字段构成一一对应关系。
     */
    private Long loginAccountId;

    /**
     * 管理员全名
     */
    private String name;

    /**
     * department表对应的id字段
     */
    private Long departmentId;

    /**
     * 联系该管理员用的邮箱
     */
    private String email;

    /**
     * 是否已删除 0未删除 1已删除
     */
    private Integer isDeleted;

    @Serial
    private static final long serialVersionUID = 1L;
}
