package com.community.idle.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.idle.common.BusinessException;
import com.community.idle.common.JwtUtils;
import com.community.idle.common.ResultCode;
import com.community.idle.common.UserContext;
import com.community.idle.dto.LoginDTO;
import com.community.idle.dto.RegisterDTO;
import com.community.idle.entity.User;
import com.community.idle.mapper.UserMapper;
import com.community.idle.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

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
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
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
        user.setRole(0);
        user.setStatus(1);
        user.setCreditScore(new BigDecimal("100.00"));
        user.setCreditLevel("良好");
        user.setExchangeCount(0);
        user.setReleaseCount(0);
        userMapper.insert(user);
    }

    @Override
    public User getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userMapper.selectById(userId);
    }

    @Override
    public void logout() {
        UserContext.remove();
    }
}
