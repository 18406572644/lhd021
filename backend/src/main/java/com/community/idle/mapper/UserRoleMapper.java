package com.community.idle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.idle.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    int deleteByUserId(@Param("userId") Long userId);

    int batchInsert(@Param("list") List<UserRole> list);
}
