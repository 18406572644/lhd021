package com.community.idle.controller;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.Result;
import com.community.idle.dto.CreditAdjustDTO;
import com.community.idle.entity.CreditRating;
import com.community.idle.service.CreditRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户信用评级")
@RestController
@RequestMapping("/credit-rating")
public class CreditRatingController {

    private final CreditRatingService creditRatingService;

    public CreditRatingController(CreditRatingService creditRatingService) {
        this.creditRatingService = creditRatingService;
    }

    @Operation(summary = "分页查询信用记录")
    @GetMapping("/page")
    public Result<PageResult<CreditRating>> page(@ModelAttribute PageQuery query,
                                                 @RequestParam(required = false) Long userId,
                                                 @RequestParam(required = false) String changeType) {
        return Result.success(creditRatingService.page(query, userId, changeType));
    }

    @Operation(summary = "我的信用记录")
    @GetMapping("/my")
    public Result<PageResult<CreditRating>> myCreditRecord(@ModelAttribute PageQuery query) {
        return Result.success(creditRatingService.myCreditRecord(query));
    }

    @Operation(summary = "获取用户信用历史")
    @GetMapping("/user/{userId}")
    public Result<List<CreditRating>> getUserCreditHistory(@PathVariable Long userId) {
        return Result.success(creditRatingService.getUserCreditHistory(userId));
    }

    @Operation(summary = "获取信用等级配置")
    @GetMapping("/level-config")
    public Result<Map<String, Object>> getCreditLevelConfig() {
        return Result.success(creditRatingService.getCreditLevelConfig());
    }

    @Operation(summary = "重新计算用户信用等级")
    @PostMapping("/calculate/{userId}")
    public Result<Void> calculateCreditLevel(@PathVariable Long userId) {
        creditRatingService.calculateCreditLevel(userId);
        return Result.success();
    }

    @Operation(summary = "调整用户信用分")
    @PostMapping("/adjust")
    public Result<Void> adjustCredit(@Valid @RequestBody CreditAdjustDTO dto) {
        creditRatingService.adjustCredit(
            dto.getUserId(),
            dto.getChangeScore(),
            dto.getChangeType(),
            dto.getReason(),
            null,
            dto.getOperatorName()
        );
        return Result.success();
    }
}
