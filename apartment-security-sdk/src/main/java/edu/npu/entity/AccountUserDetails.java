package edu.npu.entity;

import edu.npu.common.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author : [wangminan]
 * @description : [UserDetails的实现类 LoginAccount名存实亡]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUserDetails implements UserDetails {

    private String password;

    private String username;

    private int role;

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
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public AccountUserDetails(LoginAccount loginAccount) {
        this.username = loginAccount.getUsername();
        this.password = loginAccount.getPassword();
        this.role = loginAccount.getRole();
    }
}
