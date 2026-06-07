package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.community.idle.common.StatusConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("pickup_point")
public class PickupPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonProperty("name")
    public String getName() { return this.pointName; }
    public void setName(String name) { this.pointName = name; }

    private String pointName;

    private String address;

    @JsonProperty("manager")
    public String getManager() { return this.contactPerson; }
    public void setManager(String manager) { this.contactPerson = manager; }

    private String contactPerson;

    @JsonProperty("phone")
    public String getPhone() { return this.contactPhone; }
    public void setPhone(String phone) { this.contactPhone = phone; }

    private String contactPhone;

    private String businessHours;

    @JsonProperty("remark")
    public String getRemark() { return this.pointDesc; }
    public void setRemark(String remark) { this.pointDesc = remark; }

    private String pointDesc;

    private Double longitude;

    private Double latitude;

    @JsonProperty("capacity")
    public Integer getCapacity() { return this.maxCapacity; }
    public void setCapacity(Integer capacity) { this.maxCapacity = capacity; }

    private Integer maxCapacity;

    private Integer currentStock;

    private Integer status;

    @JsonProperty("status")
    public String getStatusStr() {
        return StatusConverter.getPickupPointStatus(this.status);
    }
    public void setStatusStr(String status) {
        this.status = StatusConverter.getPickupPointStatus(status);
    }

    @JsonProperty("usageRate")
    public Integer getUsageRate() {
        if (this.maxCapacity == null || this.maxCapacity == 0) return 0;
        return (int) ((this.currentStock * 100.0) / this.maxCapacity);
    }

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

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getPointDesc() {
        return pointDesc;
    }

    public void setPointDesc(String pointDesc) {
        this.pointDesc = pointDesc;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
