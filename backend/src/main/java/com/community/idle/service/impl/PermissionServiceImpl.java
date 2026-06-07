package com.community.idle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.idle.common.BusinessException;
import com.community.idle.common.ResultCode;
import com.community.idle.entity.Permission;
import com.community.idle.mapper.PermissionMapper;
import com.community.idle.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<Permission> tree() {
        List<Permission> all = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getStatus, 1)
                .orderByAsc(Permission::getParentId, Permission::getSortOrder));
        return buildTree(all, 0L);
    }

    private List<Permission> buildTree(List<Permission> all, Long parentId) {
        List<Permission> children = all.stream()
                .filter(p -> parentId.equals(p.getParentId()))
                .collect(Collectors.toList());
        for (Permission child : children) {
            child.setChildren(buildTree(all, child.getId()));
        }
        return children;
    }

    @Override
    public List<Permission> list(Integer permissionType) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (permissionType != null) {
            wrapper.eq(Permission::getPermissionType, permissionType);
        }
        wrapper.orderByAsc(Permission::getParentId, Permission::getSortOrder);
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public Permission detail(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Permission permission) {
        Permission exist = permissionMapper.selectOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getPermissionCode, permission.getPermissionCode()));
        if (exist != null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "权限编码已存在");
        }
        if (permission.getParentId() == null) {
            permission.setParentId(0L);
        }
        if (permission.getSortOrder() == null) {
            permission.setSortOrder(0);
        }
        if (permission.getVisible() == null) {
            permission.setVisible(1);
        }
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        permissionMapper.insert(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Permission permission) {
        Permission exist = permissionMapper.selectById(permission.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        Permission codeExist = permissionMapper.selectOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getPermissionCode, permission.getPermissionCode())
                .ne(Permission::getId, permission.getId()));
        if (codeExist != null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "权限编码已存在");
        }
        permissionMapper.updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        Long count = permissionMapper.selectCount(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getParentId, id));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "存在子权限，不能删除");
        }
        permissionMapper.deleteById(id);
    }

    @Override
    public List<Permission> getUserMenus(Long userId) {
        List<Permission> all = permissionMapper.selectPermissionsByUserId(userId);
        List<Permission> menus = all.stream()
                .filter(p -> p.getPermissionType() == 1 && p.getVisible() == 1)
                .collect(Collectors.toList());
        return buildTree(menus, 0L);
    }

    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        return permissionMapper.selectPermissionCodesByUserId(userId);
    }
}
