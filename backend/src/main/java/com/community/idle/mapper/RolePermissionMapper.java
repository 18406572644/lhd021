package com.community.idle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.idle.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    int deleteByRoleId(@Param("roleId") Long roleId);

    int batchInsert(@Param("list") List<RolePermission> list);
}
