package com.community.idle.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.idle.common.*;
import com.community.idle.common.annotation.DataScope;
import com.community.idle.dto.AssignRoleDTO;
import com.community.idle.dto.LoginDTO;
import com.community.idle.dto.RegisterDTO;
import com.community.idle.entity.Permission;
import com.community.idle.entity.Role;
import com.community.idle.entity.User;
import com.community.idle.entity.UserRole;
import com.community.idle.mapper.PermissionMapper;
import com.community.idle.mapper.RoleMapper;
import com.community.idle.mapper.UserMapper;
import com.community.idle.mapper.UserRoleMapper;
import com.community.idle.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;

    public AuthServiceImpl(UserMapper userMapper, JwtUtils jwtUtils, RoleMapper roleMapper, UserRoleMapper userRoleMapper, PermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        String md5Password = SecureUtil.md5(dto.getPassword());
        if (!md5Password.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (user.getCreditScore().compareTo(new BigDecimal("60")) < 0) {
            throw new BusinessException(ResultCode.CREDIT_INSUFFICIENT);
        }

        List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
        String roleCodesStr = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.joining(","));

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getDeptId(), roleCodesStr);

        List<String> roleNames = roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
        user.setRoleNames(roleNames);
        user.setRoleIds(roles.stream().map(Role::getId).collect(Collectors.toList()));

        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
        List<String> permissions = loadUserPermissions(user.getId());
        List<Permission> menuTree = buildMenuTree(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", EntityConverter.convertUser(user));
        result.put("roles", roleCodes);
        result.put("permissions", permissions);
        result.put("menuTree", menuTree);
        return result;
    }

    private List<String> loadUserPermissions(Long userId) {
        return permissionMapper.selectPermissionCodesByUserId(userId);
    }

    private List<Permission> buildMenuTree(Long userId) {
        List<Permission> allPermissions = permissionMapper.selectPermissionsByUserId(userId);
        List<Permission> menuPermissions = allPermissions.stream()
                .filter(p -> p.getPermissionType() != null && p.getPermissionType() == 1)
                .sorted(Comparator.comparing(Permission::getSortOrder))
                .collect(Collectors.toList());

        Map<Long, Permission> permissionMap = new HashMap<>();
        for (Permission p : menuPermissions) {
            permissionMap.put(p.getId(), p);
        }

        List<Permission> rootMenus = new ArrayList<>();
        for (Permission p : menuPermissions) {
            if (p.getParentId() == null || p.getParentId() == 0) {
                rootMenus.add(p);
            } else {
                Permission parent = permissionMap.get(p.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(p);
                    parent.getChildren().sort(Comparator.comparing(Permission::getSortOrder));
                }
            }
        }
        return rootMenus;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(SecureUtil.md5(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        user.setCreditScore(new BigDecimal("100.00"));
        user.setCreditLevel("良好");
        user.setExchangeCount(0);
        user.setReleaseCount(0);
        userMapper.insert(user);

        Role normalRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, "NORMAL_USER"));
        if (normalRole != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(normalRole.getId());
            userRole.setCreateTime(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public User getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = userMapper.selectById(userId);
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        List<String> roleNames = roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
        user.setRoleNames(roleNames);
        user.setRoleIds(roles.stream().map(Role::getId).collect(Collectors.toList()));
        return EntityConverter.convertUser(user);
    }

    @Override
    @DataScope(businessType = "USER", tableAlias = "u", columnName = "dept_id", userScope = true, userColumnName = "id")
    public List<User> listAllUsers() {
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
            List<String> roleNames = roles.stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());
            user.setRoleNames(roleNames);
            user.setRoleIds(roles.stream().map(Role::getId).collect(Collectors.toList()));
        }
        return EntityConverter.convertUserList(users);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(AssignRoleDTO dto) {
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        userRoleMapper.deleteByUserId(dto.getUserId());

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            List<UserRole> userRoles = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            for (Long roleId : dto.getRoleIds()) {
                UserRole userRole = new UserRole();
                userRole.setUserId(dto.getUserId());
                userRole.setRoleId(roleId);
                userRole.setCreateTime(now);
                userRoles.add(userRole);
            }
            userRoleMapper.batchInsert(userRoles);
        }
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    public void logout() {
        UserContext.remove();
        PermissionContext.clear();
    }
}
