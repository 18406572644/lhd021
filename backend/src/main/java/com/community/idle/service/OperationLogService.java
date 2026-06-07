package com.community.idle.service;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.entity.OperationLog;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

public interface OperationLogService {

    PageResult<OperationLog> page(PageQuery query, Long operatorId, String operatorName,
                                  String operationType, LocalDateTime startTime, LocalDateTime endTime);

    OperationLog detail(Long id);

    void export(Long operatorId, String operatorName, String operationType,
                LocalDateTime startTime, LocalDateTime endTime, HttpServletResponse response);

    void saveLog(OperationLog log);

    String generateConfirmToken();

    boolean validateConfirmToken(String token);
}
