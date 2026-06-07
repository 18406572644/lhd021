package com.community.idle.controller;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.Result;
import com.community.idle.common.StatusConverter;
import com.community.idle.common.annotation.RequirePermission;
import com.community.idle.entity.PickupPoint;
import com.community.idle.service.PickupPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "自提点管理")
@RestController
@RequestMapping("/pickup-point")
public class PickupPointController {

    private final PickupPointService pickupPointService;

    public PickupPointController(PickupPointService pickupPointService) {
        this.pickupPointService = pickupPointService;
    }

    @Operation(summary = "新增自提点")
    @PostMapping
    @RequirePermission("pickup_point_add")
    public Result<Void> add(@Valid @RequestBody PickupPoint pickupPoint) {
        pickupPointService.add(pickupPoint);
        return Result.success();
    }

    @Operation(summary = "更新自提点")
    @PutMapping
    @RequirePermission("pickup_point_edit")
    public Result<Void> update(@Valid @RequestBody PickupPoint pickupPoint) {
        pickupPointService.update(pickupPoint);
        return Result.success();
    }

    @Operation(summary = "删除自提点")
    @DeleteMapping("/{id}")
    @RequirePermission("pickup_point_delete")
    public Result<Void> delete(@PathVariable Long id) {
        pickupPointService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取自提点详情")
    @GetMapping("/{id}")
    public Result<PickupPoint> detail(@PathVariable Long id) {
        return Result.success(pickupPointService.detail(id));
    }

    @Operation(summary = "分页查询自提点")
    @GetMapping("/page")
    @RequirePermission("pickup_point_list")
    public Result<PageResult<PickupPoint>> page(@ModelAttribute PageQuery query,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String status) {
        Integer statusInt = StatusConverter.getPickupPointStatus(status);
        return Result.success(pickupPointService.page(query, keyword, statusInt));
    }

    @Operation(summary = "获取所有启用的自提点")
    @GetMapping("/list")
    public Result<List<PickupPoint>> listAll() {
        return Result.success(pickupPointService.listAll());
    }

    @Operation(summary = "启用自提点")
    @PostMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        pickupPointService.enable(id);
        return Result.success();
    }

    @Operation(summary = "停用自提点")
    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        pickupPointService.disable(id);
        return Result.success();
    }
}
