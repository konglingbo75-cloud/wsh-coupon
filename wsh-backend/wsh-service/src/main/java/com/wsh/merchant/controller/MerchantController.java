package com.wsh.merchant.controller;

import com.wsh.common.core.result.R;
import com.wsh.merchant.dto.MerchantEmployeeRequest;
import com.wsh.merchant.dto.MerchantEmployeeResponse;
import com.wsh.merchant.dto.MerchantRegisterRequest;
import com.wsh.merchant.dto.MerchantResponse;
import com.wsh.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商户管理", description = "商户入驻、信息查询、员工管理")
@RestController
@RequestMapping("/v1/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "商户入驻申请",
            description = "提交商户基本信息和门店信息，创建待审核的商户记录，当前登录用户自动成为管理员")
    @PostMapping("/register")
    public R<MerchantResponse> register(@Valid @RequestBody MerchantRegisterRequest request) {
        return R.ok(merchantService.register(request));
    }

    @Operation(summary = "查询我的商户",
            description = "根据当前登录用户的 openid 查询关联的商户信息")
    @GetMapping("/mine")
    public R<MerchantResponse> getMyMerchant() {
        return R.ok(merchantService.getMyMerchant());
    }

    @Operation(summary = "添加员工",
            description = "商户管理员为商户添加员工（收银员/服务员），员工通过手机号关联")
    @PostMapping("/employees")
    public R<MerchantEmployeeResponse> addEmployee(@Valid @RequestBody MerchantEmployeeRequest request) {
        return R.ok(merchantService.addEmployee(request));
    }

    @Operation(summary = "查询员工列表",
            description = "查询指定商户下所有在职员工")
    @GetMapping("/{merchantId}/employees")
    public R<List<MerchantEmployeeResponse>> listEmployees(
            @Parameter(description = "商户ID") @PathVariable Long merchantId) {
        return R.ok(merchantService.listEmployees(merchantId));
    }
}
