package com.community.idle.controller;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.Result;
import com.community.idle.common.annotation.RequirePermission;
import com.community.idle.dto.RoleDTO;
import com.community.idle.entity.Permission;
import com.community.idle.entity.Role;
import com.community.idle.service.PermissionService;
import com.community.idle.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "系统管理")
@RestController
@RequestMapping("/system")
@RequirePermission(roles = "SUPER_ADMIN")
public class SystemController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    public SystemController(RoleService roleService, PermissionService permissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    @Operation(summary = "分页查询角色")
    @GetMapping("/role/page")
    @RequirePermission("system_role_list")
    public Result<PageResult<Role>> pageRoles(@ModelAttribute PageQuery query,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer status) {
        return Result.success(roleService.page(query, keyword, status));
    }

    @Operation(summary = "获取所有角色列表")
    @GetMapping("/role/list")
    @RequirePermission("system_role_list")
    public Result<List<Role>> listRoles() {
        return Result.success(roleService.listAll());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/role/{id}")
    @RequirePermission("system_role_list")
    public Result<Role> getRoleDetail(@PathVariable Long id) {
        return Result.success(roleService.detail(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping("/role")
    @RequirePermission("system_role_add")
    public Result<Void> addRole(@Valid @RequestBody RoleDTO dto) {
        roleService.add(dto);
        return Result.success();
    }

    @Operation(summary = "更新角色")
    @PutMapping("/role")
    @RequirePermission("system_role_edit")
    public Result<Void> updateRole(@Valid @RequestBody RoleDTO dto) {
        roleService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/role/{id}")
    @RequirePermission("system_role_delete")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "分配权限给角色")
    @PostMapping("/role/{id}/permissions")
    @RequirePermission("system_role_assign_permission")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> permissionIds = body.get("permissionIds");
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    @Operation(summary = "获取角色权限ID列表")
    @GetMapping("/role/{id}/permissions")
    @RequirePermission("system_role_list")
    public Result<Map<String, Object>> getRolePermissions(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("permissionIds", roleService.getPermissionIds(id));
        result.put("dataPermissionIds", roleService.getDataPermissionIds(id, "PICKUP_POINT"));
        return Result.success(result);
    }

    @Operation(summary = "分配数据权限给角色")
    @PostMapping("/role/{id}/data-permissions")
    @RequirePermission("system_role_assign_permission")
    public Result<Void> assignDataPermissions(@PathVariable Long id,
                                               @RequestParam String businessType,
                                               @RequestBody Map<String, List<Long>> body) {
        List<Long> businessIds = body.get("businessIds");
        roleService.assignDataPermissions(id, businessType, businessIds);
        return Result.success();
    }

    @Operation(summary = "获取权限树")
    @GetMapping("/permission/tree")
    @RequirePermission("system_permission_list")
    public Result<List<Permission>> getPermissionTree() {
        return Result.success(permissionService.tree());
    }

    @Operation(summary = "获取权限列表")
    @GetMapping("/permission/list")
    @RequirePermission("system_permission_list")
    public Result<List<Permission>> listPermissions(@RequestParam(required = false) Integer permissionType) {
        return Result.success(permissionService.list(permissionType));
    }

    @Operation(summary = "获取权限详情")
    @GetMapping("/permission/{id}")
    @RequirePermission("system_permission_list")
    public Result<Permission> getPermissionDetail(@PathVariable Long id) {
        return Result.success(permissionService.detail(id));
    }

    @Operation(summary = "新增权限")
    @PostMapping("/permission")
    @RequirePermission("system_permission_add")
    public Result<Void> addPermission(@Valid @RequestBody Permission permission) {
        permissionService.add(permission);
        return Result.success();
    }

    @Operation(summary = "更新权限")
    @PutMapping("/permission")
    @RequirePermission("system_permission_edit")
    public Result<Void> updatePermission(@Valid @RequestBody Permission permission) {
        permissionService.update(permission);
        return Result.success();
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/permission/{id}")
    @RequirePermission("system_permission_delete")
    public Result<Void> deletePermission(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/permission/menus")
    public Result<List<Permission>> getUserMenus() {
        Long userId = com.community.idle.common.UserContext.getUserId();
        return Result.success(permissionService.getUserMenus(userId));
    }
}
