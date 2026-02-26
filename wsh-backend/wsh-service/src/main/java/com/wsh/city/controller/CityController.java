package com.wsh.city.controller;

import com.wsh.city.dto.CityItem;
import com.wsh.city.dto.CityListResponse;
import com.wsh.city.dto.LocateCityRequest;
import com.wsh.city.service.CityService;
import com.wsh.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 城市公共接口（无需登录）
 */
@Tag(name = "城市服务", description = "城市列表、GPS定位")
@RestController
@RequestMapping("/v1/public")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @Operation(summary = "获取城市列表", description = "获取已开放的城市列表，包含热门城市和按拼音分组的全部城市")
    @GetMapping("/cities")
    public R<CityListResponse> getCities() {
        return R.ok(cityService.getCityList());
    }

    @Operation(summary = "GPS定位识别城市", description = "根据经纬度定位最近的已开放城市")
    @PostMapping("/locate-city")
    public R<CityItem> locateCity(@Valid @RequestBody LocateCityRequest request) {
        CityItem city = cityService.locateCity(request.getLongitude(), request.getLatitude());
        if (city == null) {
            return R.fail("未找到对应城市");
        }
        return R.ok(city);
    }

    @Operation(summary = "根据城市名称获取城市", description = "根据城市名称获取城市信息")
    @GetMapping("/cities/{cityName}")
    public R<CityItem> getCityByName(@PathVariable String cityName) {
        CityItem city = cityService.getCityByName(cityName);
        if (city == null) {
            return R.fail("城市不存在或未开放");
        }
        return R.ok(city);
    }
}
