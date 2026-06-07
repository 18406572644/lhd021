package com.community.idle.controller;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.Result;
import com.community.idle.common.StatusConverter;
import com.community.idle.common.annotation.OperationLog;
import com.community.idle.common.annotation.RequirePermission;
import com.community.idle.common.enums.OperationType;
import com.community.idle.dto.ExchangeApplyDTO;
import com.community.idle.entity.ExchangeApply;
import com.community.idle.service.ExchangeApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "互换申请管理")
@RestController
@RequestMapping("/exchange-apply")
public class ExchangeApplyController {

    private final ExchangeApplyService exchangeApplyService;

    public ExchangeApplyController(ExchangeApplyService exchangeApplyService) {
        this.exchangeApplyService = exchangeApplyService;
    }

    @Operation(summary = "提交互换申请")
    @PostMapping
    public Result<Void> apply(@Valid @RequestBody ExchangeApplyDTO dto) {
        exchangeApplyService.apply(dto);
        return Result.success();
    }

    @Operation(summary = "审核通过")
    @PostMapping("/{id}/approve")
    @OperationLog(type = OperationType.EXCHANGE_APPROVE, targetType = "EXCHANGE_APPLY")
    public Result<Void> approve(@PathVariable Long id, @RequestParam(required = false) String remark) {
        exchangeApplyService.approve(id, remark);
        return Result.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/{id}/reject")
    @OperationLog(type = OperationType.EXCHANGE_REJECT, targetType = "EXCHANGE_APPLY")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String rejectReason) {
        exchangeApplyService.reject(id, rejectReason);
        return Result.success();
    }

    @Operation(summary = "取消申请")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        exchangeApplyService.cancel(id);
        return Result.success();
    }

    @Operation(summary = "确认完成")
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        exchangeApplyService.complete(id);
        return Result.success();
    }

    @Operation(summary = "分页查询申请列表（管理员）")
    @GetMapping("/page")
    @RequirePermission("exchange_apply_list")
    public Result<PageResult<ExchangeApply>> page(@ModelAttribute PageQuery query,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String keyword) {
        Integer statusInt = StatusConverter.getExchangeStatus(status);
        return Result.success(exchangeApplyService.page(query, statusInt, keyword));
    }

    @Operation(summary = "我发起的申请")
    @GetMapping("/my-apply")
    public Result<PageResult<ExchangeApply>> myApply(@ModelAttribute PageQuery query,
                                                     @RequestParam(required = false) String status) {
        Integer statusInt = StatusConverter.getExchangeStatus(status);
        return Result.success(exchangeApplyService.myApply(query, statusInt));
    }

    @Operation(summary = "我收到的申请")
    @GetMapping("/my-receive")
    public Result<PageResult<ExchangeApply>> myReceive(@ModelAttribute PageQuery query,
                                                       @RequestParam(required = false) String status) {
        Integer statusInt = StatusConverter.getExchangeStatus(status);
        return Result.success(exchangeApplyService.myReceive(query, statusInt));
    }

    @Operation(summary = "申请详情")
    @GetMapping("/{id}")
    public Result<ExchangeApply> detail(@PathVariable Long id) {
        return Result.success(exchangeApplyService.detail(id));
    }

    @Operation(summary = "各状态数量统计")
    @GetMapping("/status-count")
    public Result<Map<String, Object>> getStatusCount() {
        return Result.success(exchangeApplyService.getStatusCount());
    }
}
