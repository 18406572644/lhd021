package com.community.idle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExchangeApplyDTO implements Serializable {

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotNull(message = "申请数量不能为空")
    @Positive(message = "申请数量必须大于0")
    private Integer applyQuantity;

    @NotBlank(message = "申请原因不能为空")
    private String applyReason;

    private String exchangeItemDesc;

    @NotBlank(message = "联系电话不能为空")
    private String applicantPhone;
}
