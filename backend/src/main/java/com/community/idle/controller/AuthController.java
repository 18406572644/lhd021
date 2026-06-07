package com.community.idle.controller;

import com.community.idle.common.Result;
import com.community.idle.common.annotation.OperationLog;
import com.community.idle.common.annotation.RequirePermission;
import com.community.idle.common.enums.OperationType;
import com.community.idle.dto.AssignRoleDTO;
import com.community.idle.dto.LoginDTO;
import com.community.idle.dto.RegisterDTO;
import com.community.idle.entity.Role;
import com.community.idle.entity.User;
import com.community.idle.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }

    @Operation(summary = "获取所有用户列表")
    @GetMapping("/users")
    @RequirePermission("system_user_list")
    public Result<List<User>> listUsers() {
        return Result.success(authService.listAllUsers());
    }

    @Operation(summary = "为用户分配角色")
    @PostMapping("/user/{userId}/roles")
    @RequirePermission("system_user_assign_role")
    @OperationLog(type = OperationType.ROLE_ASSIGN, targetType = "USER")
    public Result<Void> assignRoles(@PathVariable Long userId, @Valid @RequestBody AssignRoleDTO dto) {
        dto.setUserId(userId);
        authService.assignRoles(dto);
        return Result.success();
    }

    @Operation(summary = "禁用用户")
    @PostMapping("/user/{userId}/disable")
    @RequirePermission("system_user_edit")
    @OperationLog(type = OperationType.USER_DISABLE, targetType = "USER")
    public Result<Void> disableUser(@PathVariable Long userId) {
        authService.updateUserStatus(userId, 0);
        return Result.success();
    }

    @Operation(summary = "启用用户")
    @PostMapping("/user/{userId}/enable")
    @RequirePermission("system_user_edit")
    @OperationLog(type = OperationType.USER_ENABLE, targetType = "USER")
    public Result<Void> enableUser(@PathVariable Long userId) {
        authService.updateUserStatus(userId, 1);
        return Result.success();
    }

    @Operation(summary = "获取用户角色列表")
    @GetMapping("/user/{userId}/roles")
    @RequirePermission("system_user_list")
    public Result<List<Role>> getUserRoles(@PathVariable Long userId) {
        return Result.success(authService.getUserRoles(userId));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
