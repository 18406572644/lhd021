package com.community.idle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class ClaimRecordDTO implements Serializable {

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotNull(message = "领用数量不能为空")
    @Positive(message = "领用数量必须大于0")
    private Integer claimQuantity;

    @NotBlank(message = "联系电话不能为空")
    private String claimUserPhone;

    private Long exchangeApplyId;

    private String remark;
}
