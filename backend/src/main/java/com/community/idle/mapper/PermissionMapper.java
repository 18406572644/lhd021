package com.community.idle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.idle.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    List<Permission> selectPermissionsByUserId(@Param("userId") Long userId);

    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    List<String> selectApiPermissionsByUserId(@Param("userId") Long userId);
}
