package com.community.idle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class IdleItemDTO implements Serializable {

    @NotBlank(message = "物品名称不能为空")
    private String itemName;

    @NotBlank(message = "物品描述不能为空")
    private String itemDesc;

    @NotBlank(message = "物品分类不能为空")
    private String category;

    private String itemImages;

    @NotNull(message = "物品数量不能为空")
    @Positive(message = "物品数量必须大于0")
    private Integer quantity;

    private BigDecimal originalValue;

    @NotNull(message = "物品成色不能为空")
    private Integer conditionLevel;

    @NotNull(message = "自提点不能为空")
    private Long pickupPointId;

    private String remark;
}
