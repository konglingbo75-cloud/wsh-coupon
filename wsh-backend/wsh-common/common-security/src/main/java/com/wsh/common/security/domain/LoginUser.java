package com.wsh.common.security.domain;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 当前登录用户信息（SecurityContext 中存储的对象）
 */
@Data
public class LoginUser implements UserDetails {

    private Long userId;
    private String openid;
    private Integer role;
    private String phone;
    
    /** 商户ID（商户员工登录时设置） */
    private Long merchantId;
    
    /** 员工ID（商户员工登录时设置） */
    private Long employeeId;

    /** 管理员ID（平台管理员登录时设置） */
    private Long adminId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = switch (role) {
            case 1 -> "ROLE_MERCHANT_ADMIN";
            case 2 -> "ROLE_MERCHANT_STAFF";
            case 10 -> "ROLE_PLATFORM_ADMIN";
            default -> "ROLE_CONSUMER";
        };
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
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
