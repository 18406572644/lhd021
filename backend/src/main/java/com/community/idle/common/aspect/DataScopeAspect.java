package com.community.idle.common.aspect;

import com.community.idle.common.PermissionContext;
import com.community.idle.common.UserContext;
import com.community.idle.common.annotation.DataScope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
public class DataScopeAspect {

    private static final Logger log = LoggerFactory.getLogger(DataScopeAspect.class);

    private static final ThreadLocal<DataScopeContext> DATA_SCOPE_HOLDER = new ThreadLocal<>();

    @Pointcut("@annotation(com.community.idle.common.annotation.DataScope)")
    public void dataScopePointCut() {
    }

    @Around("dataScopePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        DataScope dataScope = getDataScopeAnnotation(point);
        if (dataScope == null || UserContext.isAdmin()) {
            return point.proceed();
        }

        try {
            Integer dataScopeType = PermissionContext.getDataScopeType();
            DataScopeContext context = buildDataScopeContext(dataScope, dataScopeType);
            DATA_SCOPE_HOLDER.set(context);
            return point.proceed();
        } finally {
            DATA_SCOPE_HOLDER.remove();
        }
    }

    private DataScope getDataScopeAnnotation(ProceedingJoinPoint point) {
        try {
            String methodName = point.getSignature().getName();
            Class<?> targetClass = point.getTarget().getClass();
            Class<?>[] parameterTypes = ((org.aspectj.lang.reflect.MethodSignature) point.getSignature()).getParameterTypes();
            java.lang.reflect.Method method = targetClass.getMethod(methodName, parameterTypes);
            return method.getAnnotation(DataScope.class);
        } catch (Exception e) {
            log.error("获取DataScope注解失败", e);
            return null;
        }
    }

    private DataScopeContext buildDataScopeContext(DataScope dataScope, Integer dataScopeType) {
        DataScopeContext context = new DataScopeContext();
        context.setTableAlias(dataScope.tableAlias());
        context.setColumnName(dataScope.columnName());
        context.setUserScope(dataScope.userScope());
        context.setUserColumnName(dataScope.userColumnName());
        context.setDataScopeType(dataScopeType);

        if (dataScopeType == 5) {
            String businessType = dataScope.businessType();
            Set<Long> businessIds = PermissionContext.getDataScopeIds(businessType);
            context.setBusinessIds(businessIds);
        }

        context.setUserId(UserContext.getUserId());
        return context;
    }

    public static DataScopeContext getContext() {
        return DATA_SCOPE_HOLDER.get();
    }

    public static void clear() {
        DATA_SCOPE_HOLDER.remove();
    }

    public static class DataScopeContext {
        private String tableAlias;
        private String columnName;
        private boolean userScope;
        private String userColumnName;
        private Integer dataScopeType;
        private Set<Long> businessIds;
        private Long userId;

        public String getTableAlias() {
            return tableAlias;
        }

        public void setTableAlias(String tableAlias) {
            this.tableAlias = tableAlias;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public boolean isUserScope() {
            return userScope;
        }

        public void setUserScope(boolean userScope) {
            this.userScope = userScope;
        }

        public String getUserColumnName() {
            return userColumnName;
        }

        public void setUserColumnName(String userColumnName) {
            this.userColumnName = userColumnName;
        }

        public Integer getDataScopeType() {
            return dataScopeType;
        }

        public void setDataScopeType(Integer dataScopeType) {
            this.dataScopeType = dataScopeType;
        }

        public Set<Long> getBusinessIds() {
            return businessIds;
        }

        public void setBusinessIds(Set<Long> businessIds) {
            this.businessIds = businessIds;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}
