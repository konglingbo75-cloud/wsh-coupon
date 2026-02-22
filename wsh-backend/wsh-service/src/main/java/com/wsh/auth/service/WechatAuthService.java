package com.wsh.auth.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.auth.dto.WechatLoginRequest;
import com.wsh.auth.dto.WechatLoginResponse;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.security.util.JwtUtil;
import com.wsh.domain.entity.MerchantEmployee;
import com.wsh.domain.entity.User;
import com.wsh.domain.mapper.MerchantEmployeeMapper;
import com.wsh.domain.mapper.UserMapper;
import com.wsh.integration.wechat.WechatApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 微信登录认证服务
 * 流程：wx.login(code) → code2Session(openid) → 查库/创建用户 → 颁发 JWT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatAuthService {

    private final WechatApiClient wechatApiClient;
    private final UserMapper userMapper;
    private final MerchantEmployeeMapper merchantEmployeeMapper;
    private final JwtUtil jwtUtil;

    /**
     * 微信小程序登录
     */
    @Transactional
    public WechatLoginResponse login(WechatLoginRequest request) {
        // 1. 调用微信 code2Session 获取 openid
        JSONObject sessionInfo = wechatApiClient.code2Session(request.getCode());
        String openid = sessionInfo.getStr("openid");
        String unionid = sessionInfo.getStr("unionid");

        if (!StringUtils.hasText(openid)) {
            throw new BusinessException(401, "微信登录失败：无法获取openid");
        }

        // 2. 查询用户是否已存在
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

        boolean isNewUser = (user == null);

        // 3. 新用户：自动注册
        if (isNewUser) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setStatus(0); // 正常
            userMapper.insert(user);
            log.info("新用户注册成功: userId={}, openid={}", user.getUserId(), openid);
        } else {
            // 更新 unionid（可能之前没有）
            if (StringUtils.hasText(unionid) && !unionid.equals(user.getUnionid())) {
                user.setUnionid(unionid);
                userMapper.updateById(user);
            }
        }

        // 4. 获取手机号（如果前端传了 phoneCode）
        if (StringUtils.hasText(request.getPhoneCode())) {
            try {
                String accessToken = wechatApiClient.getAccessToken();
                String phone = wechatApiClient.getPhoneNumber(accessToken, request.getPhoneCode());
                if (StringUtils.hasText(phone) && !phone.equals(user.getPhone())) {
                    user.setPhone(phone);
                    userMapper.updateById(user);
                }
            } catch (Exception e) {
                log.warn("获取手机号失败，跳过: {}", e.getMessage());
            }
        }

        // 5. 判断用户角色（是否为商户员工）
        int role = determineUserRole(openid);

        // 6. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getUserId(), openid, role);

        // 7. 构建响应
        return WechatLoginResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .role(role)
                .isNewUser(isNewUser)
                .build();
    }

    /**
     * 根据 openid 判断用户角色
     * 优先级：商户管理员 > 商户员工 > 普通消费者
     */
    private int determineUserRole(String openid) {
        MerchantEmployee employee = merchantEmployeeMapper.selectOne(
                new LambdaQueryWrapper<MerchantEmployee>()
                        .eq(MerchantEmployee::getOpenid, openid)
                        .eq(MerchantEmployee::getStatus, 1)
                        .last("LIMIT 1"));

        if (employee == null) {
            return Constants.ROLE_CONSUMER;
        }

        return employee.getRole() == 1
                ? Constants.ROLE_MERCHANT_ADMIN
                : Constants.ROLE_MERCHANT_STAFF;
    }
}
