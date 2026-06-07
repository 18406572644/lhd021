package com.community.idle.common;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.community.idle.common.aspect.DataScopeAspect;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.*;

@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DataScopeHandler implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(DataScopeHandler.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        DataScopeAspect.DataScopeContext context = DataScopeAspect.getContext();
        if (context == null || UserContext.isAdmin()) {
            return invocation.proceed();
        }

        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        String originalSql = boundSql.getSql();

        String newSql = buildDataScopeSql(originalSql, context);
        if (newSql != null) {
            metaObject.setValue("delegate.boundSql.sql", newSql);
        }

        return invocation.proceed();
    }

    private String buildDataScopeSql(String originalSql, DataScopeAspect.DataScopeContext context) {
        try {
            Integer dataScopeType = context.getDataScopeType();
            Long userId = context.getUserId();

            List<String> conditions = new ArrayList<>();

            if (dataScopeType == 4) {
                if (context.isUserScope()) {
                    String columnName = buildColumnName(context.getTableAlias(), context.getUserColumnName());
                    conditions.add(columnName + " = " + userId);
                }
            } else if (dataScopeType == 5) {
                Set<Long> businessIds = context.getBusinessIds();
                if (businessIds != null && !businessIds.isEmpty()) {
                    String columnName = buildColumnName(context.getTableAlias(), context.getColumnName());
                    String ids = String.join(",", businessIds.stream().map(String::valueOf).toList());
                    conditions.add(columnName + " IN (" + ids + ")");
                } else if (context.isUserScope()) {
                    String columnName = buildColumnName(context.getTableAlias(), context.getUserColumnName());
                    conditions.add(columnName + " = " + userId);
                }
            }

            if (conditions.isEmpty()) {
                return null;
            }

            String dataScopeCondition = String.join(" AND ", conditions);
            return addWhereCondition(originalSql, dataScopeCondition);
        } catch (Exception e) {
            log.error("构建数据权限SQL失败", e);
            return null;
        }
    }

    private String addWhereCondition(String originalSql, String condition) {
        String upperSql = originalSql.toUpperCase().trim();
        
        int whereIndex = upperSql.indexOf(" WHERE ");
        int orderByIndex = upperSql.indexOf(" ORDER BY ");
        int limitIndex = upperSql.indexOf(" LIMIT ");
        int groupByIndex = upperSql.indexOf(" GROUP BY ");
        int havingIndex = upperSql.indexOf(" HAVING ");

        int insertPosition;
        String insertClause;

        if (whereIndex > 0) {
            insertPosition = whereIndex + " WHERE ".length();
            insertClause = "(" + condition + ") AND ";
        } else {
            int endOfFrom = findEndOfFrom(upperSql);
            if (endOfFrom < 0) {
                return originalSql;
            }
            insertPosition = endOfFrom;
            insertClause = " WHERE (" + condition + ")";
        }

        StringBuilder sb = new StringBuilder(originalSql);
        sb.insert(insertPosition, insertClause);
        return sb.toString();
    }

    private int findEndOfFrom(String upperSql) {
        int fromIndex = upperSql.indexOf(" FROM ");
        if (fromIndex < 0) {
            return -1;
        }

        int searchPos = fromIndex + " FROM ".length();
        
        int orderByIndex = upperSql.indexOf(" ORDER BY ", searchPos);
        int limitIndex = upperSql.indexOf(" LIMIT ", searchPos);
        int groupByIndex = upperSql.indexOf(" GROUP BY ", searchPos);
        int whereIndex = upperSql.indexOf(" WHERE ", searchPos);

        List<Integer> positions = new ArrayList<>();
        if (orderByIndex > 0) positions.add(orderByIndex);
        if (limitIndex > 0) positions.add(limitIndex);
        if (groupByIndex > 0) positions.add(groupByIndex);
        if (whereIndex > 0) positions.add(whereIndex);

        if (positions.isEmpty()) {
            return upperSql.length();
        }

        return Collections.min(positions);
    }

    private String buildColumnName(String tableAlias, String columnName) {
        if (tableAlias != null && !tableAlias.isEmpty()) {
            return tableAlias + "." + columnName;
        }
        return columnName;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
