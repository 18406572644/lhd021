package com.community.idle.common;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.community.idle.common.aspect.DataScopeAspect;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
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
            Select select = (Select) CCJSqlParserUtil.parse(originalSql);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

            List<Expression> conditions = new ArrayList<>();

            Integer dataScopeType = context.getDataScopeType();
            Long userId = context.getUserId();

            if (dataScopeType == 4) {
                if (context.isUserScope()) {
                    EqualsTo equalsTo = new EqualsTo();
                    equalsTo.setLeftExpression(new Column(buildColumnName(context.getTableAlias(), context.getUserColumnName())));
                    equalsTo.setRightExpression(new LongValue(userId));
                    conditions.add(equalsTo);
                }
            } else if (dataScopeType == 5) {
                Set<Long> businessIds = context.getBusinessIds();
                if (businessIds != null && !businessIds.isEmpty()) {
                    InExpression inExpression = new InExpression();
                    inExpression.setLeftExpression(new Column(buildColumnName(context.getTableAlias(), context.getColumnName())));
                    ExpressionList expressionList = new ExpressionList();
                    List<Expression> expressions = new ArrayList<>();
                    for (Long id : businessIds) {
                        expressions.add(new LongValue(id));
                    }
                    expressionList.setExpressions(expressions);
                    inExpression.setRightExpression(expressionList);
                    conditions.add(inExpression);
                } else if (context.isUserScope()) {
                    EqualsTo equalsTo = new EqualsTo();
                    equalsTo.setLeftExpression(new Column(buildColumnName(context.getTableAlias(), context.getUserColumnName())));
                    equalsTo.setRightExpression(new LongValue(userId));
                    conditions.add(equalsTo);
                }
            }

            if (!conditions.isEmpty()) {
                Expression where = plainSelect.getWhere();
                Expression expression = conditions.get(0);
                for (int i = 1; i < conditions.size(); i++) {
                    expression = new AndExpression(expression, conditions.get(i));
                }
                Parenthesis parenthesis = new Parenthesis(expression);

                if (where == null) {
                    plainSelect.setWhere(parenthesis);
                } else {
                    plainSelect.setWhere(new AndExpression(where, parenthesis));
                }
            }

            return select.toString();
        } catch (JSQLParserException e) {
            log.error("解析SQL失败", e);
            return null;
        }
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
