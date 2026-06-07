package com.community.idle.interceptor;

import com.community.idle.common.*;
import com.community.idle.entity.Role;
import com.community.idle.mapper.DataPermissionMapper;
import com.community.idle.mapper.PermissionMapper;
import com.community.idle.mapper.RoleMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final DataPermissionMapper dataPermissionMapper;

    public TokenInterceptor(JwtUtils jwtUtils, RoleMapper roleMapper, PermissionMapper permissionMapper, DataPermissionMapper dataPermissionMapper) {
        this.jwtUtils = jwtUtils;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.dataPermissionMapper = dataPermissionMapper;
    }

    @Value("${jwt.header}")
    private String header;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(header);
        if (token == null || token.isEmpty()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);
        Long deptId = jwtUtils.getDeptIdFromToken(token);
        String roleCodesStr = jwtUtils.getRoleCodesFromToken(token);

        Set<String> roleCodes = new HashSet<>();
        if (roleCodesStr != null && !roleCodesStr.isEmpty()) {
            roleCodes = new HashSet<>(Arrays.asList(roleCodesStr.split(",")));
        }

        UserContext.CurrentUser currentUser = new UserContext.CurrentUser(userId, username, deptId, roleCodes);
        UserContext.set(currentUser);

        loadUserPermissions(userId, roleCodes);

        return true;
    }

    private void loadUserPermissions(Long userId, Set<String> roleCodes) {
        List<String> permissionCodes = permissionMapper.selectPermissionCodesByUserId(userId);
        PermissionContext.setPermissions(new HashSet<>(permissionCodes));

        PermissionContext.setRoles(roleCodes);

        List<String> apiPermissions = permissionMapper.selectApiPermissionsByUserId(userId);
        PermissionContext.setApiPermissions(new HashSet<>(apiPermissions));

        Map<String, Set<Long>> dataScopeMap = new HashMap<>();
        Set<String> businessTypes = new HashSet<>(Arrays.asList("PICKUP_POINT", "USER"));
        for (String businessType : businessTypes) {
            List<Long> businessIds = dataPermissionMapper.selectBusinessIdsByUserIdAndBusinessType(userId, businessType);
            if (!businessIds.isEmpty()) {
                dataScopeMap.put(businessType, new HashSet<>(businessIds));
            }
        }
        PermissionContext.setDataScope(dataScopeMap);

        Integer dataScopeType = determineDataScopeType(userId);
        PermissionContext.setDataScopeType(dataScopeType);
    }

    private Integer determineDataScopeType(Long userId) {
        if (UserContext.isAdmin()) {
            return 1;
        }
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        if (roles.isEmpty()) {
            return 4;
        }
        int minScope = 4;
        for (Role role : roles) {
            if (role.getDataScope() != null && role.getDataScope() < minScope) {
                minScope = role.getDataScope();
            }
        }
        return minScope;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
        PermissionContext.clear();
    }
}
