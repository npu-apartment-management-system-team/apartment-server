package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录账号
 * @TableName login_account
 */
@TableName(value ="login_account")
@Data
public class LoginAccount implements Serializable {
    /**
     * 用户唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 在客户端应当是用户的手机号，在管理端可以做一点变通
     */
    private String username;

    /**
     * 密码,使用BCrypt加密
     */
    private String password;

    /**
     * 用户角色
     */
    private Integer role;

    /**
     * 账号是否已删除 0未删除 1已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
