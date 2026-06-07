package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.idle.common.StatusConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("exchange_apply")
public class ExchangeApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyNo;

    private Long itemId;

    private String itemName;

    private String itemImages;

    private Long itemOwnerId;

    private String itemOwnerName;

    private Long applicantId;

    private String applicantName;

    private String applicantPhone;

    private Integer applyQuantity;

    private String applyReason;

    private String exchangeItemDesc;

    private Integer status;

    private Long auditUserId;

    private String auditUserName;

    private LocalDateTime auditTime;

    private String auditRemark;

    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @JsonIgnore
    private Integer deleted;

    @JsonProperty("applyNo")
    public String getApplyNo() { return this.applyNo; }

    @JsonProperty("targetItemId")
    public Long getTargetItemId() { return this.itemId; }
    public void setTargetItemId(Long id) { this.itemId = id; }

    @JsonProperty("itemName")
    public String getItemName() { return this.itemName; }

    @JsonProperty("targetItemName")
    public String getTargetItemName() { return this.itemName; }
    public void setTargetItemName(String name) { this.itemName = name; }

    @JsonProperty("itemOwnerId")
    public Long getItemOwnerId() { return this.itemOwnerId; }

    @JsonProperty("targetUserId")
    public Long getTargetUserId() { return this.itemOwnerId; }
    public void setTargetUserId(Long id) { this.itemOwnerId = id; }

    @JsonProperty("itemOwnerName")
    public String getItemOwnerName() { return this.itemOwnerName; }

    @JsonProperty("targetUserName")
    public String getTargetUserName() { return this.itemOwnerName; }
    public void setTargetUserName(String name) { this.itemOwnerName = name; }

    @JsonProperty("applicantId")
    public Long getApplicantId() { return this.applicantId; }

    @JsonProperty("applicantName")
    public String getApplicantName() { return this.applicantName; }

    @JsonProperty("contactPhone")
    public String getContactPhone() { return this.applicantPhone; }
    public void setContactPhone(String phone) { this.applicantPhone = phone; }

    @JsonProperty("quantity")
    public Integer getQuantity() { return this.applyQuantity; }
    public void setQuantity(Integer quantity) { this.applyQuantity = quantity; }

    @JsonProperty("remark")
    public String getRemark() { return this.applyReason; }
    public void setRemark(String remark) { this.applyReason = remark; }

    @JsonProperty("myItemName")
    public String getMyItemName() { return this.exchangeItemDesc; }
    public void setMyItemName(String name) { this.exchangeItemDesc = name; }

    @JsonProperty("myItemId")
    public Long getMyItemId() { return this.itemId; }
    public void setMyItemId(Long id) { this.itemId = id; }

    @JsonProperty("status")
    public String getStatusText() { return StatusConverter.getExchangeStatus(this.status); }
    public void setStatusText(String status) { this.status = StatusConverter.getExchangeStatus(status); }

    public Integer getStatusValue() { return this.status; }

    @JsonProperty("auditorName")
    public String getAuditorName() { return this.auditUserName; }

    @JsonProperty("auditTime")
    public LocalDateTime getAuditTime() { return this.auditTime; }

    @JsonProperty("rejectReason")
    public String getRejectReason() { return this.rejectReason; }

    @JsonProperty("applyTime")
    public LocalDateTime getApplyTime() { return this.createTime; }

    @JsonProperty("claimRecordId")
    public Long getClaimRecordId() {
        return this.status == 1 || this.status == 3 ? this.id : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setApplyNo(String applyNo) {
        this.applyNo = applyNo;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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

    public void setItemOwnerId(Long itemOwnerId) {
        this.itemOwnerId = itemOwnerId;
    }

    public void setItemOwnerName(String itemOwnerName) {
        this.itemOwnerName = itemOwnerName;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantPhone() {
        return applicantPhone;
    }

    public void setApplicantPhone(String applicantPhone) {
        this.applicantPhone = applicantPhone;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getAuditUserId() {
        return auditUserId;
    }

    public void setAuditUserId(Long auditUserId) {
        this.auditUserId = auditUserId;
    }

    public String getAuditUserName() {
        return auditUserName;
    }

    public void setAuditUserName(String auditUserName) {
        this.auditUserName = auditUserName;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
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
