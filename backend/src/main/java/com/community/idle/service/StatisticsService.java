package com.community.idle.service;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.entity.MonthlyStatistics;

import java.util.Map;

public interface StatisticsService {
    MonthlyStatistics generateMonthly(String month);
    MonthlyStatistics generateCurrentMonth();
    PageResult<MonthlyStatistics> page(PageQuery query);
    MonthlyStatistics detail(Long id);
    MonthlyStatistics getByMonth(String month);
    Map<String, Object> getDashboard();
    Map<String, Object> getTrendData(Integer months);
}
