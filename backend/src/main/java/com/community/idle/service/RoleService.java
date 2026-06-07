package com.community.idle.service;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.dto.RoleDTO;
import com.community.idle.entity.Role;

import java.util.List;

public interface RoleService {
    PageResult<Role> page(PageQuery query, String keyword, Integer status);

    List<Role> listAll();

    Role detail(Long id);

    void add(RoleDTO dto);

    void update(RoleDTO dto);

    void delete(Long id);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    List<Long> getPermissionIds(Long roleId);

    void assignDataPermissions(Long roleId, String businessType, List<Long> businessIds);

    List<Long> getDataPermissionIds(Long roleId, String businessType);
}
