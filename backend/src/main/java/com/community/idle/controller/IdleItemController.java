package com.community.idle.controller;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.Result;
import com.community.idle.common.StatusConverter;
import com.community.idle.common.annotation.RequirePermission;
import com.community.idle.dto.IdleItemDTO;
import com.community.idle.entity.IdleItem;
import com.community.idle.service.IdleItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "闲置物品管理")
@RestController
@RequestMapping("/idle-item")
public class IdleItemController {

    private final IdleItemService idleItemService;

    public IdleItemController(IdleItemService idleItemService) {
        this.idleItemService = idleItemService;
    }

    @Operation(summary = "发布闲置物品")
    @PostMapping
    @RequirePermission("idle_item_add")
    public Result<Void> publish(@Valid @RequestBody IdleItemDTO dto) {
        idleItemService.publish(dto);
        return Result.success();
    }

    @Operation(summary = "更新闲置物品")
    @PutMapping("/{id}")
    @RequirePermission("idle_item_edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody IdleItemDTO dto) {
        idleItemService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "分页查询闲置物品")
    @GetMapping("/page")
    @RequirePermission("idle_item_list")
    public Result<PageResult<IdleItem>> page(@ModelAttribute PageQuery query,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(required = false) String status) {
        Integer statusInt = StatusConverter.getItemStatus(status);
        return Result.success(idleItemService.page(query, keyword, category, statusInt));
    }

    @Operation(summary = "获取物品详情")
    @GetMapping("/{id}")
    public Result<IdleItem> detail(@PathVariable Long id) {
        return Result.success(idleItemService.detail(id));
    }

    @Operation(summary = "下架物品")
    @PostMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        idleItemService.offline(id);
        return Result.success();
    }

    @Operation(summary = "批量下架物品")
    @PostMapping("/offline/batch")
    public Result<Void> offlineBatch(@RequestBody List<Long> ids) {
        idleItemService.offlineBatch(ids);
        return Result.success();
    }

    @Operation(summary = "我发布的物品")
    @GetMapping("/my")
    public Result<PageResult<IdleItem>> myPublish(@ModelAttribute PageQuery query,
                                                  @RequestParam(required = false) String status) {
        Integer statusInt = StatusConverter.getItemStatus(status);
        return Result.success(idleItemService.myPublish(query, statusInt));
    }

    @Operation(summary = "获取物品分类列表")
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(idleItemService.getCategories());
    }
}
