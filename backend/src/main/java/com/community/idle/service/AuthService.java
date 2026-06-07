package com.community.idle.service;

import com.community.idle.dto.AssignRoleDTO;
import com.community.idle.dto.LoginDTO;
import com.community.idle.dto.RegisterDTO;
import com.community.idle.entity.Role;
import com.community.idle.entity.User;

import java.util.List;
import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginDTO dto);
    void register(RegisterDTO dto);
    User getCurrentUser();
    List<User> listAllUsers();
    void assignRoles(AssignRoleDTO dto);
    List<Role> getUserRoles(Long userId);
    void logout();
    void updateUserStatus(Long userId, Integer status);
    User getUserById(Long userId);
}
