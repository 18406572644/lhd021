package com.community.idle.controller;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.Result;
import com.community.idle.dto.ClaimRecordDTO;
import com.community.idle.entity.ClaimRecord;
import com.community.idle.service.ClaimRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "物品领用记录")
@RestController
@RequestMapping("/claim-record")
@RequiredArgsConstructor
public class ClaimRecordController {

    private final ClaimRecordService claimRecordService;

    @Operation(summary = "创建领用单")
    @PostMapping
    public Result<ClaimRecord> createClaim(@Valid @RequestBody ClaimRecordDTO dto) {
        return Result.success(claimRecordService.createClaim(dto));
    }

    @Operation(summary = "确认领取")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmPickup(@PathVariable Long id,
                                      @RequestParam String pickupCode,
                                      @RequestParam String operatorName) {
        claimRecordService.confirmPickup(id, pickupCode, operatorName);
        return Result.success();
    }

    @Operation(summary = "取消领用")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelClaim(@PathVariable Long id) {
        claimRecordService.cancelClaim(id);
        return Result.success();
    }

    @Operation(summary = "分页查询领用记录（管理员）")
    @GetMapping("/page")
    public Result<PageResult<ClaimRecord>> page(@ModelAttribute PageQuery query,
                                                @RequestParam(required = false) Integer pickupStatus,
                                                @RequestParam(required = false) String keyword) {
        return Result.success(claimRecordService.page(query, pickupStatus, keyword));
    }

    @Operation(summary = "我的领用记录")
    @GetMapping("/my")
    public Result<PageResult<ClaimRecord>> myClaim(@ModelAttribute PageQuery query,
                                                   @RequestParam(required = false) Integer pickupStatus) {
        return Result.success(claimRecordService.myClaim(query, pickupStatus));
    }

    @Operation(summary = "领用记录详情")
    @GetMapping("/{id}")
    public Result<ClaimRecord> detail(@PathVariable Long id) {
        return Result.success(claimRecordService.detail(id));
    }
}
