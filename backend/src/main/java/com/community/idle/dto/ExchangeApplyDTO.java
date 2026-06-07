package com.community.idle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

public class ExchangeApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @JsonProperty("targetItemId")
    public Long getTargetItemId() { return this.itemId; }
    public void setTargetItemId(Long id) { this.itemId = id; }

    @NotNull(message = "申请数量不能为空")
    @Positive(message = "申请数量必须大于0")
    private Integer applyQuantity;

    @JsonProperty("quantity")
    public Integer getQuantity() { return this.applyQuantity; }
    public void setQuantity(Integer quantity) { this.applyQuantity = quantity; }

    @NotBlank(message = "申请原因不能为空")
    private String applyReason;

    @JsonProperty("remark")
    public String getRemark() { return this.applyReason; }
    public void setRemark(String remark) { this.applyReason = remark; }

    private String exchangeItemDesc;

    @JsonProperty("myItemName")
    public String getMyItemName() { return this.exchangeItemDesc; }
    public void setMyItemName(String name) { this.exchangeItemDesc = name; }

    @JsonProperty("myItemId")
    private Long myItemId;

    @NotBlank(message = "联系电话不能为空")
    private String applicantPhone;

    @JsonProperty("contactPhone")
    public String getContactPhone() { return this.applicantPhone; }
    public void setContactPhone(String phone) { this.applicantPhone = phone; }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getApplyQuantity() {
        return applyQuantity;
    }

    public void setApplyQuantity(Integer applyQuantity) {
        this.applyQuantity = applyQuantity;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public String getExchangeItemDesc() {
        return exchangeItemDesc;
    }

    public void setExchangeItemDesc(String exchangeItemDesc) {
        this.exchangeItemDesc = exchangeItemDesc;
    }

    public Long getMyItemId() {
        return myItemId;
    }

    public void setMyItemId(Long myItemId) {
        this.myItemId = myItemId;
    }

    public String getApplicantPhone() {
        return applicantPhone;
    }

    public void setApplicantPhone(String applicantPhone) {
        this.applicantPhone = applicantPhone;
    }
}
