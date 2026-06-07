package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("monthly_statistics")
public class MonthlyStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonProperty("statMonth")
    public String getStatMonth() { return this.statisticsMonth; }

    private String statisticsMonth;

    @JsonProperty("newUsers")
    public Integer getNewUsers() { return this.newUserCount; }

    private Integer newUserCount;

    @JsonProperty("activeUsers")
    public Integer getActiveUsers() { return this.activeUserCount; }

    private Integer activeUserCount;

    @JsonProperty("totalPublished")
    public Integer getTotalPublished() { return this.releaseItemCount; }

    private Integer releaseItemCount;

    @JsonProperty("totalExchanges")
    public Integer getTotalExchanges() { return this.exchangeApplyCount; }

    private Integer exchangeApplyCount;

    @JsonProperty("approvedExchanges")
    public Integer getApprovedExchanges() { return this.exchangeSuccessCount; }

    private Integer exchangeSuccessCount;

    @JsonProperty("successRate")
    public Integer getSuccessRateInt() {
        return this.exchangeSuccessRate != null ? this.exchangeSuccessRate.intValue() : 0;
    }

    private BigDecimal exchangeSuccessRate;

    @JsonProperty("totalClaimed")
    public Integer getTotalClaimed() { return this.claimCount; }

    private Integer claimCount;

    @JsonProperty("totalArchives")
    public Integer getTotalArchives() { return this.archiveItemCount; }

    private Integer archiveItemCount;

    @JsonProperty("activityLevel")
    public String getActivityLevel() {
        if (this.activeUserCount == null) return "低";
        if (this.activeUserCount >= 100) return "高";
        if (this.activeUserCount >= 50) return "中";
        return "低";
    }

    @JsonProperty("publishGrowth")
    public Integer getPublishGrowth() { return 0; }

    @JsonProperty("exchangeGrowth")
    public Integer getExchangeGrowth() { return 0; }

    private BigDecimal totalValue;

    private Integer creditUpgradeCount;

    private Integer creditDowngradeCount;

    private Integer pickupPointCount;

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

    public String getStatisticsMonth() {
        return statisticsMonth;
    }

    public void setStatisticsMonth(String statisticsMonth) {
        this.statisticsMonth = statisticsMonth;
    }

    public Integer getNewUserCount() {
        return newUserCount;
    }

    public void setNewUserCount(Integer newUserCount) {
        this.newUserCount = newUserCount;
    }

    public Integer getActiveUserCount() {
        return activeUserCount;
    }

    public void setActiveUserCount(Integer activeUserCount) {
        this.activeUserCount = activeUserCount;
    }

    public Integer getReleaseItemCount() {
        return releaseItemCount;
    }

    public void setReleaseItemCount(Integer releaseItemCount) {
        this.releaseItemCount = releaseItemCount;
    }

    public Integer getExchangeApplyCount() {
        return exchangeApplyCount;
    }

    public void setExchangeApplyCount(Integer exchangeApplyCount) {
        this.exchangeApplyCount = exchangeApplyCount;
    }

    public Integer getExchangeSuccessCount() {
        return exchangeSuccessCount;
    }

    public void setExchangeSuccessCount(Integer exchangeSuccessCount) {
        this.exchangeSuccessCount = exchangeSuccessCount;
    }

    public BigDecimal getExchangeSuccessRate() {
        return exchangeSuccessRate;
    }

    public void setExchangeSuccessRate(BigDecimal exchangeSuccessRate) {
        this.exchangeSuccessRate = exchangeSuccessRate;
    }

    public Integer getClaimCount() {
        return claimCount;
    }

    public void setClaimCount(Integer claimCount) {
        this.claimCount = claimCount;
    }

    public Integer getArchiveItemCount() {
        return archiveItemCount;
    }

    public void setArchiveItemCount(Integer archiveItemCount) {
        this.archiveItemCount = archiveItemCount;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public Integer getCreditUpgradeCount() {
        return creditUpgradeCount;
    }

    public void setCreditUpgradeCount(Integer creditUpgradeCount) {
        this.creditUpgradeCount = creditUpgradeCount;
    }

    public Integer getCreditDowngradeCount() {
        return creditDowngradeCount;
    }

    public void setCreditDowngradeCount(Integer creditDowngradeCount) {
        this.creditDowngradeCount = creditDowngradeCount;
    }

    public Integer getPickupPointCount() {
        return pickupPointCount;
    }

    public void setPickupPointCount(Integer pickupPointCount) {
        this.pickupPointCount = pickupPointCount;
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
