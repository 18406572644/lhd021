package com.community.idle.controller;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.Result;
import com.community.idle.entity.ItemArchive;
import com.community.idle.service.ItemArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "闲置下架归档")
@RestController
@RequestMapping("/item-archive")
public class ItemArchiveController {

    private final ItemArchiveService itemArchiveService;

    public ItemArchiveController(ItemArchiveService itemArchiveService) {
        this.itemArchiveService = itemArchiveService;
    }

    @Operation(summary = "手动归档物品")
    @PostMapping("/archive/{itemId}")
    public Result<Void> archive(@PathVariable Long itemId, @RequestParam String reason) {
        itemArchiveService.archive(itemId, reason);
        return Result.success();
    }

    @Operation(summary = "批量归档物品")
    @PostMapping("/archive/batch")
    public Result<Void> archiveBatch(@RequestBody List<Long> itemIds, @RequestParam String reason) {
        itemArchiveService.archiveBatch(itemIds, reason);
        return Result.success();
    }

    @Operation(summary = "系统自动归档")
    @PostMapping("/auto-archive")
    public Result<Void> autoArchive() {
        itemArchiveService.autoArchive();
        return Result.success();
    }

    @Operation(summary = "分页查询归档记录")
    @GetMapping("/page")
    public Result<PageResult<ItemArchive>> page(@ModelAttribute PageQuery query,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String archiveType) {
        return Result.success(itemArchiveService.page(query, keyword, archiveType));
    }

    @Operation(summary = "归档详情")
    @GetMapping("/{id}")
    public Result<ItemArchive> detail(@PathVariable Long id) {
        return Result.success(itemArchiveService.detail(id));
    }

    @Operation(summary = "恢复已归档物品")
    @PostMapping("/restore/{id}")
    public Result<ItemArchive> restore(@PathVariable Long id) {
        return Result.success(itemArchiveService.restore(id));
    }
}
