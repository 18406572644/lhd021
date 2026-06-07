package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.idle.common.StatusConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("claim_record")
public class ClaimRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonProperty("claimNo")
    private String recordNo;

    private Long itemId;

    private String itemName;

    @JsonProperty("imageUrl")
    private String itemImages;

    private Long itemOwnerId;

    @JsonProperty("ownerName")
    private String itemOwnerName;

    private Long claimUserId;

    @JsonProperty("claimantName")
    public String getClaimantName() { return this.claimUserName; }

    private String claimUserName;

    @JsonProperty("contactPhone")
    public String getContactPhone() { return this.claimUserPhone; }

    private String claimUserPhone;

    @JsonProperty("quantity")
    public Integer getQuantity() { return this.claimQuantity; }

    private Integer claimQuantity;

    private Long exchangeApplyId;

    private Long pickupPointId;

    private String pickupPointName;

    @JsonProperty("claimTime")
    public LocalDateTime getClaimTime() { return this.claimTime != null ? this.claimTime : this.createTime; }

    private LocalDateTime claimTime;

    @JsonProperty("pickupTime")
    private LocalDateTime pickupTime;

    private Integer pickupStatus;

    @JsonProperty("status")
    public String getStatus() {
        return StatusConverter.getPickupStatus(this.pickupStatus);
    }
    public void setStatus(String status) {
        this.pickupStatus = StatusConverter.getPickupStatus(status);
    }

    private String pickupCode;

    private String operatorName;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @JsonIgnore
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecordNo() {
        return recordNo;
    }

    public void setRecordNo(String recordNo) {
        this.recordNo = recordNo;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemImages() {
        return itemImages;
    }

    public void setItemImages(String itemImages) {
        this.itemImages = itemImages;
    }

    public Long getItemOwnerId() {
        return itemOwnerId;
    }

    public void setItemOwnerId(Long itemOwnerId) {
        this.itemOwnerId = itemOwnerId;
    }

    public String getItemOwnerName() {
        return itemOwnerName;
    }

    public void setItemOwnerName(String itemOwnerName) {
        this.itemOwnerName = itemOwnerName;
    }

    public Long getClaimUserId() {
        return claimUserId;
    }

    public void setClaimUserId(Long claimUserId) {
        this.claimUserId = claimUserId;
    }

    public String getClaimUserName() {
        return claimUserName;
    }

    public void setClaimUserName(String claimUserName) {
        this.claimUserName = claimUserName;
    }

    public String getClaimUserPhone() {
        return claimUserPhone;
    }

    public void setClaimUserPhone(String claimUserPhone) {
        this.claimUserPhone = claimUserPhone;
    }

    public Integer getClaimQuantity() {
        return claimQuantity;
    }

    public void setClaimQuantity(Integer claimQuantity) {
        this.claimQuantity = claimQuantity;
    }

    public Long getExchangeApplyId() {
        return exchangeApplyId;
    }

    public void setExchangeApplyId(Long exchangeApplyId) {
        this.exchangeApplyId = exchangeApplyId;
    }

    public Long getPickupPointId() {
        return pickupPointId;
    }

    public void setPickupPointId(Long pickupPointId) {
        this.pickupPointId = pickupPointId;
    }

    public String getPickupPointName() {
        return pickupPointName;
    }

    public void setPickupPointName(String pickupPointName) {
        this.pickupPointName = pickupPointName;
    }

    public void setClaimTime(LocalDateTime claimTime) {
        this.claimTime = claimTime;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(LocalDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public Integer getPickupStatus() {
        return pickupStatus;
    }

    public void setPickupStatus(Integer pickupStatus) {
        this.pickupStatus = pickupStatus;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
