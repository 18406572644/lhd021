package com.community.idle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.idle.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    List<Role> selectRolesByUserId(@Param("userId") Long userId);

    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectDataPermissionIdsByRoleId(@Param("roleId") Long roleId, @Param("businessType") String businessType);
}
