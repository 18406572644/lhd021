package com.community.idle.common.aspect;

import cn.hutool.core.util.StrUtil;
import com.community.idle.common.BusinessException;
import com.community.idle.common.DataCompareUtil;
import com.community.idle.common.ResultCode;
import com.community.idle.common.UserContext;
import com.community.idle.common.enums.OperationType;
import com.community.idle.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogService operationLogService;

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Pointcut("@annotation(com.community.idle.common.annotation.OperationLog)")
    public void operationLogPointCut() {
    }

    @Around("operationLogPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        com.community.idle.common.annotation.OperationLog operationLogAnnotation = getOperationLogAnnotation(point);
        if (operationLogAnnotation == null) {
            return point.proceed();
        }

        OperationType operationType = operationLogAnnotation.type();
        boolean isSensitive = operationType.isSensitive();
        boolean requireConfirm = operationLogAnnotation.requireConfirm() || isSensitive;

        if (requireConfirm) {
            validateConfirm(point, operationLogAnnotation);
        }

        Object beforeData = null;
        Long targetId = null;
        String targetName = null;
        String targetType = operationLogAnnotation.targetType();

        try {
            targetId = extractTargetId(point, operationLogAnnotation.targetIdParam());
            targetName = extractTargetName(point, operationLogAnnotation.targetNameParam());
            if (operationLogAnnotation.recordData() && targetId != null) {
                beforeData = getBeforeData(point, targetId);
            }
        } catch (Exception e) {
            log.warn("获取操作前数据失败", e);
        }

        Object result = point.proceed();

        try {
            Object afterData = null;
            if (operationLogAnnotation.recordData() && targetId != null) {
                afterData = getAfterData(point, targetId);
            }

            String changeSummary = DataCompareUtil.compareAndGetSummary(beforeData, afterData);

            com.community.idle.entity.OperationLog logEntity = new com.community.idle.entity.OperationLog();
            logEntity.setOperationType(operationType.getCode());
            logEntity.setOperationName(StrUtil.isNotBlank(operationLogAnnotation.name())
                    ? operationLogAnnotation.name() : operationType.getName());
            logEntity.setOperatorId(UserContext.getUserId());
            logEntity.setOperatorName(UserContext.getUsername());
            logEntity.setOperationTime(LocalDateTime.now());
            logEntity.setOperationIp(getClientIp());
            logEntity.setTargetId(targetId);
            logEntity.setTargetType(targetType);
            logEntity.setTargetName(targetName);
            logEntity.setBeforeData(DataCompareUtil.toJson(beforeData));
            logEntity.setAfterData(DataCompareUtil.toJson(afterData));
            logEntity.setChangeSummary(changeSummary);
            logEntity.setIsSensitive(isSensitive ? 1 : 0);
            logEntity.setConfirmed(requireConfirm ? 1 : 0);

            operationLogService.saveLog(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }

        return result;
    }

    private void validateConfirm(ProceedingJoinPoint point, com.community.idle.common.annotation.OperationLog annotation) {
        if (annotation.confirmThreshold() > 0 && StrUtil.isNotBlank(annotation.confirmThresholdParam())) {
            BigDecimal threshold = BigDecimal.valueOf(annotation.confirmThreshold());
            BigDecimal paramValue = extractBigDecimalParam(point, annotation.confirmThresholdParam());
            if (paramValue != null && paramValue.abs().compareTo(threshold) < 0) {
                return;
            }
        }

        HttpServletRequest request = getRequest();
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        String confirmToken = request.getHeader("X-Confirm-Token");
        if (StrUtil.isBlank(confirmToken)) {
            String token = operationLogService.generateConfirmToken();
            throw new BusinessException(ResultCode.CONFIRM_REQUIRED.getCode(),
                    ResultCode.CONFIRM_REQUIRED.getMessage() + ":" + token);
        }

        if (!operationLogService.validateConfirmToken(confirmToken)) {
            throw new BusinessException(ResultCode.CONFIRM_TOKEN_INVALID);
        }
    }

    private com.community.idle.common.annotation.OperationLog getOperationLogAnnotation(ProceedingJoinPoint point) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            return signature.getMethod().getAnnotation(com.community.idle.common.annotation.OperationLog.class);
        } catch (Exception e) {
            log.error("获取OperationLog注解失败", e);
            return null;
        }
    }

    private Long extractTargetId(ProceedingJoinPoint point, String targetIdParam) {
        if (StrUtil.isBlank(targetIdParam)) {
            return null;
        }
        Object[] args = point.getArgs();
        MethodSignature signature = (MethodSignature) point.getSignature();
        String[] paramNames = signature.getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            if (targetIdParam.equals(paramNames[i])) {
                Object arg = args[i];
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (arg instanceof String) {
                    try {
                        return Long.parseLong((String) arg);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }

        for (Object arg : args) {
            if (arg != null && isEntityOrDto(arg.getClass())) {
                try {
                    Field idField = arg.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object idValue = idField.get(arg);
                    if (idValue instanceof Long) {
                        return (Long) idValue;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // ignore
                }
            }
        }

        return null;
    }

    private String extractTargetName(ProceedingJoinPoint point, String targetNameParam) {
        if (StrUtil.isBlank(targetNameParam)) {
            return null;
        }
        Object[] args = point.getArgs();
        MethodSignature signature = (MethodSignature) point.getSignature();
        String[] paramNames = signature.getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            if (targetNameParam.equals(paramNames[i])) {
                Object arg = args[i];
                if (arg instanceof String) {
                    return (String) arg;
                }
            }
        }

        for (Object arg : args) {
            if (arg != null && isEntityOrDto(arg.getClass())) {
                try {
                    for (String fieldName : new String[]{"itemName", "pointName", "username", "nickname", "roleName", "permissionName", "name"}) {
                        try {
                            Field nameField = arg.getClass().getDeclaredField(fieldName);
                            nameField.setAccessible(true);
                            Object nameValue = nameField.get(arg);
                            if (nameValue instanceof String) {
                                return (String) nameValue;
                            }
                        } catch (NoSuchFieldException e) {
                            // continue to next field
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return null;
    }

    private BigDecimal extractBigDecimalParam(ProceedingJoinPoint point, String paramName) {
        if (StrUtil.isBlank(paramName)) {
            return null;
        }
        Object[] args = point.getArgs();
        MethodSignature signature = (MethodSignature) point.getSignature();
        String[] paramNames = signature.getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            if (paramName.equals(paramNames[i])) {
                Object arg = args[i];
                if (arg instanceof BigDecimal) {
                    return (BigDecimal) arg;
                } else if (arg instanceof Integer) {
                    return BigDecimal.valueOf((Integer) arg);
                } else if (arg instanceof Double) {
                    return BigDecimal.valueOf((Double) arg);
                } else if (arg instanceof String) {
                    try {
                        return new BigDecimal((String) arg);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }

        for (Object arg : args) {
            if (arg != null && isEntityOrDto(arg.getClass())) {
                try {
                    Field field = arg.getClass().getDeclaredField(paramName);
                    field.setAccessible(true);
                    Object value = field.get(arg);
                    if (value instanceof BigDecimal) {
                        return (BigDecimal) value;
                    } else if (value instanceof Integer) {
                        return BigDecimal.valueOf((Integer) value);
                    } else if (value instanceof Double) {
                        return BigDecimal.valueOf((Double) value);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return null;
    }

    private Object getBeforeData(ProceedingJoinPoint point, Long targetId) {
        String className = point.getTarget().getClass().getSimpleName();
        try {
            Method method = point.getTarget().getClass().getMethod("getById", Long.class);
            return method.invoke(point.getTarget(), targetId);
        } catch (NoSuchMethodException e) {
            try {
                Method detailMethod = point.getTarget().getClass().getMethod("detail", Long.class);
                return detailMethod.invoke(point.getTarget(), targetId);
            } catch (Exception ex) {
                log.debug("获取操作前数据方法不存在: {}", className);
                return null;
            }
        } catch (Exception e) {
            log.warn("获取操作前数据失败: {}", className, e);
            return null;
        }
    }

    private Object getAfterData(ProceedingJoinPoint point, Long targetId) {
        return getBeforeData(point, targetId);
    }

    private boolean isEntityOrDto(Class<?> clazz) {
        String packageName = clazz.getPackage().getName();
        return packageName.contains("entity") || packageName.contains("dto");
    }

    private String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
