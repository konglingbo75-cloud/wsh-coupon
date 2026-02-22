package com.wsh.common.security.util;

import com.wsh.common.security.domain.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类：获取当前登录用户信息
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户 ID
     */
    public static Long getUserId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static Integer getRole() {
        LoginUser user = getLoginUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 判断当前用户是否为商户管理员
     */
    public static boolean isMerchantAdmin() {
        Integer role = getRole();
        return role != null && role == 1;
    }

    /**
     * 判断当前用户是否为商户员工
     */
    public static boolean isMerchantStaff() {
        Integer role = getRole();
        return role != null && (role == 1 || role == 2);
    }

    /**
     * 获取当前用户的 openid
     */
    public static String getOpenid() {
        LoginUser user = getLoginUser();
        return user != null ? user.getOpenid() : null;
    }

    /**
     * 获取当前用户关联的商户ID（商户员工登录时）
     */
    public static Long getMerchantId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getMerchantId() : null;
    }

    /**
     * 获取当前用户的员工ID（商户员工登录时）
     */
    public static Long getEmployeeId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getEmployeeId() : null;
    }

    /**
     * 获取当前用户手机号
     */
    public static String getPhone() {
        LoginUser user = getLoginUser();
        return user != null ? user.getPhone() : null;
    }

    /**
     * 判断当前用户是否为平台管理员
     */
    public static boolean isPlatformAdmin() {
        Integer role = getRole();
        return role != null && role == 10;
    }

    /**
     * 获取当前管理员ID（平台管理员登录时）
     */
    public static Long getAdminId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getAdminId() : null;
    }
}
