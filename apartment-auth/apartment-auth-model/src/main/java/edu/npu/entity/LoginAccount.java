package edu.npu.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import edu.npu.common.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 登录账号
 * @TableName login_account
 */
@TableName(value ="login_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Json转换时只需要id,username,password,role,isDeleted这些loginAccount自己的字段
// 忽略UserDetails中的其他字段
@JsonIgnoreProperties(
        value = {"accountNonExpired", "accountNonLocked",
                "credentialsNonExpired", "enabled", "authorities"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginAccount implements Serializable, UserDetails {
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer role;

    /**
     * 账号是否已删除 0未删除 1已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 2589L;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(
                Objects.requireNonNull(RoleEnum.fromValue(role)).name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isDeleted == 0;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
