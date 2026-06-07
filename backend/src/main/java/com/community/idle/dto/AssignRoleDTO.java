package com.community.idle.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AssignRoleDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
