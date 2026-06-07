package com.community.idle.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.idle.common.BusinessException;
import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.ResultCode;
import com.community.idle.common.UserContext;
import com.community.idle.common.enums.OperationType;
import com.community.idle.entity.OperationLog;
import com.community.idle.mapper.OperationLogMapper;
import com.community.idle.service.OperationLogService;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final ConcurrentHashMap<String, Long> tokenCache = new ConcurrentHashMap<>();
    private static final long TOKEN_EXPIRE_MINUTES = 5;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public PageResult<OperationLog> page(PageQuery query, Long operatorId, String operatorName,
                                         String operationType, LocalDateTime startTime, LocalDateTime endTime) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }
        Page<OperationLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<OperationLog> resultPage = operationLogMapper.selectPage(page, operatorId, operatorName, operationType, startTime, endTime);
        List<OperationLog> records = resultPage.getRecords();
        records.forEach(this::fillOperationTypeName);
        return PageResult.of(resultPage);
    }

    @Override
    public OperationLog detail(Long id) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }
        OperationLog log = operationLogMapper.selectById(id);
        if (log != null) {
            fillOperationTypeName(log);
        }
        return log;
    }

    @Override
    public void export(Long operatorId, String operatorName, String operationType,
                       LocalDateTime startTime, LocalDateTime endTime, HttpServletResponse response) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }
        List<OperationLog> list = operationLogMapper.selectExportList(operatorId, operatorName, operationType, startTime, endTime);
        list.forEach(this::fillOperationTypeName);

        response.setContentType("text/csv;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        String fileName = "operation_log_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            throw new BusinessException("导出失败");
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');
            writer.println("操作时间,操作类型,操作名称,操作人,操作IP,操作对象,变更摘要,是否敏感,是否确认");
            for (OperationLog log : list) {
                StringBuilder sb = new StringBuilder();
                sb.append(log.getOperationTime() != null ? log.getOperationTime().format(DATE_TIME_FORMATTER) : "").append(",");
                sb.append(escapeCsv(log.getOperationTypeName())).append(",");
                sb.append(escapeCsv(log.getOperationName())).append(",");
                sb.append(escapeCsv(log.getOperatorName())).append(",");
                sb.append(escapeCsv(log.getOperationIp())).append(",");
                sb.append(escapeCsv(log.getTargetName())).append(",");
                sb.append(escapeCsv(log.getChangeSummary())).append(",");
                sb.append(log.getIsSensitive() != null && log.getIsSensitive() == 1 ? "是" : "否").append(",");
                sb.append(log.getConfirmed() != null && log.getConfirmed() == 1 ? "是" : "否");
                writer.println(sb);
            }
            writer.flush();
        } catch (IOException e) {
            throw new BusinessException("导出失败");
        }
    }

    @Override
    public void saveLog(OperationLog log) {
        if (log.getCreateTime() == null) {
            log.setCreateTime(LocalDateTime.now());
        }
        operationLogMapper.insert(log);
    }

    @Override
    public String generateConfirmToken() {
        String token = IdUtil.fastSimpleUUID();
        tokenCache.put(token, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(TOKEN_EXPIRE_MINUTES));
        return token;
    }

    @Override
    public boolean validateConfirmToken(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        Long expireTime = tokenCache.get(token);
        if (expireTime == null || System.currentTimeMillis() > expireTime) {
            tokenCache.remove(token);
            return false;
        }
        tokenCache.remove(token);
        return true;
    }

    private void fillOperationTypeName(OperationLog log) {
        if (StrUtil.isNotBlank(log.getOperationType())) {
            OperationType type = OperationType.getByCode(log.getOperationType());
            log.setOperationTypeName(type.getName());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
