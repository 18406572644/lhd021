package com.community.idle.service;

import com.community.idle.dto.LoginDTO;
import com.community.idle.dto.RegisterDTO;
import com.community.idle.entity.User;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginDTO dto);
    void register(RegisterDTO dto);
    User getCurrentUser();
    void logout();
}
