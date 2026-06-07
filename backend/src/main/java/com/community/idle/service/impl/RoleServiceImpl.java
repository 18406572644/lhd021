package com.community.idle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.idle.common.BusinessException;
import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.ResultCode;
import com.community.idle.dto.RoleDTO;
import com.community.idle.entity.DataPermission;
import com.community.idle.entity.Role;
import com.community.idle.entity.RolePermission;
import com.community.idle.mapper.DataPermissionMapper;
import com.community.idle.mapper.RoleMapper;
import com.community.idle.mapper.RolePermissionMapper;
import com.community.idle.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final DataPermissionMapper dataPermissionMapper;

    public RoleServiceImpl(RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper, DataPermissionMapper dataPermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.dataPermissionMapper = dataPermissionMapper;
    }

    @Override
    public PageResult<Role> page(PageQuery query, String keyword, Integer status) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Role::getRoleName, keyword)
                    .or()
                    .like(Role::getRoleCode, keyword);
        }
        if (status != null) {
            wrapper.eq(Role::getStatus, status);
        }
        wrapper.orderByAsc(Role::getSortOrder);

        Page<Role> page = roleMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<Role> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, 1)
                .orderByAsc(Role::getSortOrder));
    }

    @Override
    public Role detail(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        role.setPermissionIds(getPermissionIds(id));
        role.setDataPermissionIds(getDataPermissionIds(id, "PICKUP_POINT"));
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RoleDTO dto) {
        Role existRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, dto.getRoleCode()));
        if (existRole != null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "角色编码已存在");
        }

        Role role = new Role();
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setRoleDesc(dto.getRoleDesc());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        role.setDataScope(dto.getDataScope() != null ? dto.getDataScope() : 4);
        role.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        roleMapper.insert(role);

        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            assignPermissions(role.getId(), dto.getPermissionIds());
        }

        if (dto.getPickupPointIds() != null && !dto.getPickupPointIds().isEmpty()) {
            assignDataPermissions(role.getId(), "PICKUP_POINT", dto.getPickupPointIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleDTO dto) {
        Role role = roleMapper.selectById(dto.getId());
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        Role existRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, dto.getRoleCode())
                .ne(Role::getId, dto.getId()));
        if (existRole != null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "角色编码已存在");
        }

        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setRoleDesc(dto.getRoleDesc());
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        if (dto.getDataScope() != null) {
            role.setDataScope(dto.getDataScope());
        }
        if (dto.getSortOrder() != null) {
            role.setSortOrder(dto.getSortOrder());
        }
        roleMapper.updateById(role);

        if (dto.getPermissionIds() != null) {
            assignPermissions(role.getId(), dto.getPermissionIds());
        }

        if (dto.getPickupPointIds() != null) {
            assignDataPermissions(role.getId(), "PICKUP_POINT", dto.getPickupPointIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if ("SUPER_ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "超级管理员角色不能删除");
        }
        roleMapper.deleteById(id);
        rolePermissionMapper.deleteByRoleId(id);
        dataPermissionMapper.deleteByRoleIdAndBusinessType(id, "PICKUP_POINT");
        dataPermissionMapper.deleteByRoleIdAndBusinessType(id, "USER");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        rolePermissionMapper.deleteByRoleId(roleId);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermission> rolePermissions = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            for (Long permissionId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rp.setCreateTime(now);
                rolePermissions.add(rp);
            }
            rolePermissionMapper.batchInsert(rolePermissions);
        }
    }

    @Override
    public List<Long> getPermissionIds(Long roleId) {
        return roleMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignDataPermissions(Long roleId, String businessType, List<Long> businessIds) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        dataPermissionMapper.deleteByRoleIdAndBusinessType(roleId, businessType);

        if (businessIds != null && !businessIds.isEmpty()) {
            List<DataPermission> dataPermissions = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            for (Long businessId : businessIds) {
                DataPermission dp = new DataPermission();
                dp.setRoleId(roleId);
                dp.setBusinessType(businessType);
                dp.setBusinessId(businessId);
                dp.setCreateTime(now);
                dataPermissions.add(dp);
            }
            dataPermissionMapper.batchInsert(dataPermissions);
        }
    }

    @Override
    public List<Long> getDataPermissionIds(Long roleId, String businessType) {
        return roleMapper.selectDataPermissionIdsByRoleId(roleId, businessType);
    }
}
