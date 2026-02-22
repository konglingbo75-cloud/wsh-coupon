package com.wsh.config;

import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "系统", description = "系统状态接口")
@RestController
public class SystemController {

    @Operation(summary = "系统状态检查")
    @GetMapping("/v1/public/status")
    public R<Map<String, Object>> status() {
        return R.ok(Map.of(
                "service", "wsh-service",
                "version", "1.0.0",
                "time", LocalDateTime.now().toString()
        ));
    }
}
