package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private String nickname;

    private String phone;

    private String email;

    private String avatar;

    private Long deptId;

    private Integer status;

    private BigDecimal creditScore;

    private String creditLevel;

    private Integer exchangeCount;

    private Integer releaseCount;

    @TableField(exist = false)
    private Integer publishCount;

    @TableField(exist = false)
    private Integer claimCount;

    @TableField(exist = false)
    private java.util.List<String> roleNames;

    @TableField(exist = false)
    private java.util.List<Long> roleIds;

    @TableField(exist = false)
    private java.util.List<String> roles;

    @TableField(exist = false)
    private java.util.List<String> permissions;

    @TableField(exist = false)
    private java.util.List<String> apiPermissions;

    @TableField(exist = false)
    private java.util.List<DataPermission> dataPermissions;

    @TableField(exist = false)
    private Integer dataScopeType;

    @TableField(exist = false)
    private String statusName;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(BigDecimal creditScore) {
        this.creditScore = creditScore;
    }

    public String getCreditLevel() {
        return creditLevel;
    }

    public void setCreditLevel(String creditLevel) {
        this.creditLevel = creditLevel;
    }

    public Integer getExchangeCount() {
        return exchangeCount;
    }

    public void setExchangeCount(Integer exchangeCount) {
        this.exchangeCount = exchangeCount;
    }

    public Integer getReleaseCount() {
        return releaseCount;
    }

    public void setReleaseCount(Integer releaseCount) {
        this.releaseCount = releaseCount;
    }

    public Integer getPublishCount() {
        return publishCount;
    }

    public void setPublishCount(Integer publishCount) {
        this.publishCount = publishCount;
    }

    public Integer getClaimCount() {
        return claimCount;
    }

    public void setClaimCount(Integer claimCount) {
        this.claimCount = claimCount;
    }

    public java.util.List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(java.util.List<String> roleNames) {
        this.roleNames = roleNames;
    }

    public java.util.List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(java.util.List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public java.util.List<String> getRoles() {
        return roles;
    }

    public void setRoles(java.util.List<String> roles) {
        this.roles = roles;
    }

    public java.util.List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(java.util.List<String> permissions) {
        this.permissions = permissions;
    }

    public java.util.List<String> getApiPermissions() {
        return apiPermissions;
    }

    public void setApiPermissions(java.util.List<String> apiPermissions) {
        this.apiPermissions = apiPermissions;
    }

    public java.util.List<DataPermission> getDataPermissions() {
        return dataPermissions;
    }

    public void setDataPermissions(java.util.List<DataPermission> dataPermissions) {
        this.dataPermissions = dataPermissions;
    }

    public Integer getDataScopeType() {
        return dataScopeType;
    }

    public void setDataScopeType(Integer dataScopeType) {
        this.dataScopeType = dataScopeType;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
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
