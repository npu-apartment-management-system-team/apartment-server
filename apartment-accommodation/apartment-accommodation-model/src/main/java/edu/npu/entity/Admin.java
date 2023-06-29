package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 管理员
 * @TableName admin
 */
@TableName(value ="admin")
@Data
public class Admin implements Serializable {
    /**
     * 管理员唯一ID
     */
    @TableId(type = IdType.AUTO)
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}