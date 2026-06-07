package com.community.idle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.idle.entity.DataPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DataPermissionMapper extends BaseMapper<DataPermission> {

    int deleteByRoleIdAndBusinessType(@Param("roleId") Long roleId, @Param("businessType") String businessType);

    int batchInsert(@Param("list") List<DataPermission> list);

    List<Long> selectBusinessIdsByUserIdAndBusinessType(@Param("userId") Long userId, @Param("businessType") String businessType);

    List<DataPermission> selectDataPermissionsByUserId(@Param("userId") Long userId);
}
