package com.wsh.admin.service;

import com.wsh.common.security.util.SecurityUtil;
import com.wsh.domain.entity.AdminOperationLog;
import com.wsh.domain.mapper.AdminOperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOperationLogService {

    private final AdminOperationLogMapper operationLogMapper;

    public void log(String module, String action, String targetId, String detail) {
        Long adminId = SecurityUtil.getAdminId();
        if (adminId == null) {
            adminId = SecurityUtil.getUserId();
        }

        AdminOperationLog opLog = new AdminOperationLog();
        opLog.setAdminId(adminId);
        opLog.setModule(module);
        opLog.setAction(action);
        opLog.setTargetId(targetId);
        opLog.setDetail(detail);
        opLog.setIp(getClientIp());
        operationLogMapper.insert(opLog);
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败: {}", e.getMessage());
        }
        return "unknown";
    }
}
