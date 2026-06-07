package com.community.idle.service;

import com.community.idle.entity.Permission;

import java.util.List;

public interface PermissionService {
    List<Permission> tree();

    List<Permission> list(Integer permissionType);

    Permission detail(Long id);

    void add(Permission permission);

    void update(Permission permission);

    void delete(Long id);

    List<Permission> getUserMenus(Long userId);

    List<String> getUserPermissionCodes(Long userId);
}
