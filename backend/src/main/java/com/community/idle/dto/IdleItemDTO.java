package com.community.idle.dto;

import com.community.idle.common.StatusConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;

public class IdleItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "物品名称不能为空")
    private String itemName;

    @JsonProperty("name")
    public String getName() { return this.itemName; }
    public void setName(String name) { this.itemName = name; }

    @NotBlank(message = "物品描述不能为空")
    private String itemDesc;

    @JsonProperty("description")
    public String getDescription() { return this.itemDesc; }
    public void setDescription(String description) { this.itemDesc = description; }

    @NotBlank(message = "物品分类不能为空")
    private String category;

    private String itemImages;

    @JsonProperty("imageUrl")
    public String getImageUrl() { return this.itemImages; }
    public void setImageUrl(String imageUrl) { this.itemImages = imageUrl; }

    @NotNull(message = "物品数量不能为空")
    @Positive(message = "物品数量必须大于0")
    private Integer quantity;

    private BigDecimal originalValue;

    @NotNull(message = "物品成色不能为空")
    private Integer conditionLevel;

    @JsonProperty("itemCondition")
    public String getItemCondition() {
        return StatusConverter.getCondition(this.conditionLevel);
    }
    public void setItemCondition(String condition) {
        this.conditionLevel = StatusConverter.getCondition(condition);
    }

    @NotNull(message = "自提点不能为空")
    private Long pickupPointId;

    private String expectedExchange;

    private String remark;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getItemImages() {
        return itemImages;
    }

    public void setItemImages(String itemImages) {
        this.itemImages = itemImages;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(BigDecimal originalValue) {
        this.originalValue = originalValue;
    }

    public Integer getConditionLevel() {
        return conditionLevel;
    }

    public void setConditionLevel(Integer conditionLevel) {
        this.conditionLevel = conditionLevel;
    }

    public Long getPickupPointId() {
        return pickupPointId;
    }

    public void setPickupPointId(Long pickupPointId) {
        this.pickupPointId = pickupPointId;
    }

    public String getExpectedExchange() {
        return expectedExchange;
    }

    public void setExpectedExchange(String expectedExchange) {
        this.expectedExchange = expectedExchange;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
