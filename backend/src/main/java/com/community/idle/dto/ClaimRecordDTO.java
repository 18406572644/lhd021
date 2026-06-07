package com.community.idle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

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

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getClaimQuantity() {
        return claimQuantity;
    }

    public void setClaimQuantity(Integer claimQuantity) {
        this.claimQuantity = claimQuantity;
    }

    public String getClaimUserPhone() {
        return claimUserPhone;
    }

    public void setClaimUserPhone(String claimUserPhone) {
        this.claimUserPhone = claimUserPhone;
    }

    public Long getExchangeApplyId() {
        return exchangeApplyId;
    }

    public void setExchangeApplyId(Long exchangeApplyId) {
        this.exchangeApplyId = exchangeApplyId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
