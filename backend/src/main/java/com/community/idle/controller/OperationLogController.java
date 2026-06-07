package com.community.idle.controller;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.Result;
import com.community.idle.common.annotation.RequirePermission;
import com.community.idle.entity.OperationLog;
import com.community.idle.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/operation-log")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/page")
    @RequirePermission("operation_log_list")
    public Result<PageResult<OperationLog>> page(@ModelAttribute PageQuery query,
                                                 @RequestParam(required = false) Long operatorId,
                                                 @RequestParam(required = false) String operatorName,
                                                 @RequestParam(required = false) String operationType,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(operationLogService.page(query, operatorId, operatorName, operationType, startTime, endTime));
    }

    @Operation(summary = "获取操作日志详情")
    @GetMapping("/{id}")
    @RequirePermission("operation_log_detail")
    public Result<OperationLog> detail(@PathVariable Long id) {
        return Result.success(operationLogService.detail(id));
    }

    @Operation(summary = "导出操作日志")
    @GetMapping("/export")
    @RequirePermission("operation_log_export")
    public void export(@RequestParam(required = false) Long operatorId,
                       @RequestParam(required = false) String operatorName,
                       @RequestParam(required = false) String operationType,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                       HttpServletResponse response) {
        operationLogService.export(operatorId, operatorName, operationType, startTime, endTime, response);
    }

    @Operation(summary = "获取二次确认令牌")
    @PostMapping("/confirm-token")
    public Result<Map<String, String>> getConfirmToken() {
        String token = operationLogService.generateConfirmToken();
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }
}
