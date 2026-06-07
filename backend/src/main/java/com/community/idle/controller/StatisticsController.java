package com.community.idle.controller;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.Result;
import com.community.idle.entity.MonthlyStatistics;
import com.community.idle.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "月度互助数据统计")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "生成指定月份统计数据")
    @PostMapping("/generate/{month}")
    public Result<MonthlyStatistics> generateMonthly(@PathVariable String month) {
        return Result.success(statisticsService.generateMonthly(month));
    }

    @Operation(summary = "生成当月统计数据")
    @PostMapping("/generate/current")
    public Result<MonthlyStatistics> generateCurrentMonth() {
        return Result.success(statisticsService.generateCurrentMonth());
    }

    @Operation(summary = "分页查询月度统计")
    @GetMapping("/page")
    public Result<PageResult<MonthlyStatistics>> page(@ModelAttribute PageQuery query) {
        return Result.success(statisticsService.page(query));
    }

    @Operation(summary = "获取统计详情")
    @GetMapping("/{id}")
    public Result<MonthlyStatistics> detail(@PathVariable Long id) {
        return Result.success(statisticsService.detail(id));
    }

    @Operation(summary = "获取指定月份统计")
    @GetMapping("/month/{month}")
    public Result<MonthlyStatistics> getByMonth(@PathVariable String month) {
        return Result.success(statisticsService.getByMonth(month));
    }

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(statisticsService.getDashboard());
    }

    @Operation(summary = "获取趋势数据")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrendData(@RequestParam(defaultValue = "6") Integer months) {
        return Result.success(statisticsService.getTrendData(months));
    }

    @Operation(summary = "获取分类统计")
    @GetMapping("/category-stats")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        return Result.success(statisticsService.getCategoryStats());
    }

    @Operation(summary = "获取信用分布")
    @GetMapping("/credit-distribution")
    public Result<Map<String, Object>> getCreditDistribution() {
        return Result.success(statisticsService.getCreditDistribution());
    }
}
