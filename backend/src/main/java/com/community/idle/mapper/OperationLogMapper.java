package com.community.idle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.idle.entity.OperationLog;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationLogMapper extends BaseMapper<OperationLog> {

    IPage<OperationLog> selectPage(Page<OperationLog> page,
                                   @Param("operatorId") Long operatorId,
                                   @Param("operatorName") String operatorName,
                                   @Param("operationType") String operationType,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    List<OperationLog> selectExportList(@Param("operatorId") Long operatorId,
                                        @Param("operatorName") String operatorName,
                                        @Param("operationType") String operationType,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);
}
