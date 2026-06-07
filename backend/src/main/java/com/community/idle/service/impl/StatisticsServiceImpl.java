package com.community.idle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.community.idle.common.*;
import com.community.idle.entity.*;
import com.community.idle.mapper.*;
import com.community.idle.service.StatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final MonthlyStatisticsMapper monthlyStatisticsMapper;
    private final UserMapper userMapper;
    private final IdleItemMapper idleItemMapper;
    private final ExchangeApplyMapper exchangeApplyMapper;
    private final ClaimRecordMapper claimRecordMapper;
    private final ItemArchiveMapper itemArchiveMapper;
    private final CreditRatingMapper creditRatingMapper;
    private final PickupPointMapper pickupPointMapper;

    public StatisticsServiceImpl(MonthlyStatisticsMapper monthlyStatisticsMapper, UserMapper userMapper, IdleItemMapper idleItemMapper, ExchangeApplyMapper exchangeApplyMapper, ClaimRecordMapper claimRecordMapper, ItemArchiveMapper itemArchiveMapper, CreditRatingMapper creditRatingMapper, PickupPointMapper pickupPointMapper) {
        this.monthlyStatisticsMapper = monthlyStatisticsMapper;
        this.userMapper = userMapper;
        this.idleItemMapper = idleItemMapper;
        this.exchangeApplyMapper = exchangeApplyMapper;
        this.claimRecordMapper = claimRecordMapper;
        this.itemArchiveMapper = itemArchiveMapper;
        this.creditRatingMapper = creditRatingMapper;
        this.pickupPointMapper = pickupPointMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonthlyStatistics generateMonthly(String month) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        MonthlyStatistics exist = monthlyStatisticsMapper.selectOne(new LambdaQueryWrapper<MonthlyStatistics>()
                .eq(MonthlyStatistics::getStatisticsMonth, month));
        int newUserCount = Math.toIntExact(userMapper.selectCount(new LambdaQueryWrapper<User>()
                .between(User::getCreateTime, startOfMonth, endOfMonth)));
        Set<Long> activeUserIds = new HashSet<>();
        List<IdleItem> items = idleItemMapper.selectList(new LambdaQueryWrapper<IdleItem>()
                .between(IdleItem::getCreateTime, startOfMonth, endOfMonth)
                .select(IdleItem::getUserId));
        items.forEach(item -> activeUserIds.add(item.getUserId()));
        List<ExchangeApply> applies = exchangeApplyMapper.selectList(new LambdaQueryWrapper<ExchangeApply>()
                .between(ExchangeApply::getCreateTime, startOfMonth, endOfMonth)
                .select(ExchangeApply::getApplicantId, ExchangeApply::getItemOwnerId));
        applies.forEach(apply -> {
            activeUserIds.add(apply.getApplicantId());
            activeUserIds.add(apply.getItemOwnerId());
        });
        int activeUserCount = activeUserIds.size();
        int releaseItemCount = Math.toIntExact(idleItemMapper.selectCount(new LambdaQueryWrapper<IdleItem>()
                .between(IdleItem::getCreateTime, startOfMonth, endOfMonth)));
        int exchangeApplyCount = Math.toIntExact(exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .between(ExchangeApply::getCreateTime, startOfMonth, endOfMonth)));
        int exchangeSuccessCount = Math.toIntExact(exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .eq(ExchangeApply::getStatus, 3)
                .between(ExchangeApply::getUpdateTime, startOfMonth, endOfMonth)));
        BigDecimal exchangeSuccessRate = exchangeApplyCount > 0
                ? BigDecimal.valueOf(exchangeSuccessCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(exchangeApplyCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        int claimCount = Math.toIntExact(claimRecordMapper.selectCount(new LambdaQueryWrapper<ClaimRecord>()
                .eq(ClaimRecord::getPickupStatus, 1)
                .between(ClaimRecord::getPickupTime, startOfMonth, endOfMonth)));
        int archiveItemCount = Math.toIntExact(itemArchiveMapper.selectCount(new LambdaQueryWrapper<ItemArchive>()
                .between(ItemArchive::getCreateTime, startOfMonth, endOfMonth)));
        List<IdleItem> monthItems = idleItemMapper.selectList(new LambdaQueryWrapper<IdleItem>()
                .between(IdleItem::getCreateTime, startOfMonth, endOfMonth)
                .select(IdleItem::getOriginalValue, IdleItem::getQuantity));
        BigDecimal totalValue = monthItems.stream()
                .map(item -> item.getOriginalValue() != null
                        ? item.getOriginalValue().multiply(BigDecimal.valueOf(item.getQuantity()))
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int creditUpgradeCount = Math.toIntExact(creditRatingMapper.selectCount(new LambdaQueryWrapper<CreditRating>()
                .eq(CreditRating::getChangeType, "ADD")
                .between(CreditRating::getCreateTime, startOfMonth, endOfMonth)));
        int creditDowngradeCount = Math.toIntExact(creditRatingMapper.selectCount(new LambdaQueryWrapper<CreditRating>()
                .eq(CreditRating::getChangeType, "SUBTRACT")
                .between(CreditRating::getCreateTime, startOfMonth, endOfMonth)));
        int pickupPointCount = Math.toIntExact(pickupPointMapper.selectCount(new LambdaQueryWrapper<PickupPoint>()
                .eq(PickupPoint::getStatus, 1)));
        MonthlyStatistics statistics;
        if (exist != null) {
            statistics = exist;
        } else {
            statistics = new MonthlyStatistics();
            statistics.setStatisticsMonth(month);
        }
        statistics.setNewUserCount(newUserCount);
        statistics.setActiveUserCount(activeUserCount);
        statistics.setReleaseItemCount(releaseItemCount);
        statistics.setExchangeApplyCount(exchangeApplyCount);
        statistics.setExchangeSuccessCount(exchangeSuccessCount);
        statistics.setExchangeSuccessRate(exchangeSuccessRate);
        statistics.setClaimCount(claimCount);
        statistics.setArchiveItemCount(archiveItemCount);
        statistics.setTotalValue(totalValue);
        statistics.setCreditUpgradeCount(creditUpgradeCount);
        statistics.setCreditDowngradeCount(creditDowngradeCount);
        statistics.setPickupPointCount(pickupPointCount);
        statistics.setRemark(yearMonth.getYear() + "年" + yearMonth.getMonthValue() + "月互助数据统计");
        if (exist != null) {
            monthlyStatisticsMapper.updateById(statistics);
        } else {
            monthlyStatisticsMapper.insert(statistics);
        }
        return EntityConverter.convertMonthlyStatistics(statistics);
    }

    @Override
    public MonthlyStatistics generateCurrentMonth() {
        String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return generateMonthly(currentMonth);
    }

    @Override
    public PageResult<MonthlyStatistics> page(PageQuery query) {
        IPage<MonthlyStatistics> page = monthlyStatisticsMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("statistics_month"))),
                new LambdaQueryWrapper<>());
        PageResult<MonthlyStatistics> result = PageResult.of(page);
        result.setList(EntityConverter.convertMonthlyStatisticsList(result.getList()));
        return result;
    }

    @Override
    public MonthlyStatistics detail(Long id) {
        MonthlyStatistics statistics = monthlyStatisticsMapper.selectById(id);
        if (statistics == null) {
            throw new BusinessException("统计记录不存在");
        }
        return EntityConverter.convertMonthlyStatistics(statistics);
    }

    @Override
    public MonthlyStatistics getByMonth(String month) {
        MonthlyStatistics statistics = monthlyStatisticsMapper.selectOne(new LambdaQueryWrapper<MonthlyStatistics>()
                .eq(MonthlyStatistics::getStatisticsMonth, month));
        if (statistics == null) {
            return generateMonthly(month);
        }
        return EntityConverter.convertMonthlyStatistics(statistics);
    }

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        Map<String, Object> totalStats = new LinkedHashMap<>();
        totalStats.put("totalUser", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1)));
        totalStats.put("totalItem", idleItemMapper.selectCount(new LambdaQueryWrapper<IdleItem>()
                .eq(IdleItem::getStatus, 1)));
        totalStats.put("totalExchange", exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .eq(ExchangeApply::getStatus, 3)));
        totalStats.put("totalClaim", claimRecordMapper.selectCount(new LambdaQueryWrapper<ClaimRecord>()
                .eq(ClaimRecord::getPickupStatus, 1)));
        dashboard.put("totalStats", totalStats);
        Map<String, Object> todayStats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);
        todayStats.put("newUser", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .between(User::getCreateTime, todayStart, todayEnd)));
        todayStats.put("newItem", idleItemMapper.selectCount(new LambdaQueryWrapper<IdleItem>()
                .between(IdleItem::getCreateTime, todayStart, todayEnd)));
        todayStats.put("newApply", exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .between(ExchangeApply::getCreateTime, todayStart, todayEnd)));
        todayStats.put("completedExchange", exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .eq(ExchangeApply::getStatus, 3)
                .between(ExchangeApply::getUpdateTime, todayStart, todayEnd)));
        dashboard.put("todayStats", todayStats);
        Map<String, Object> pendingTasks = new LinkedHashMap<>();
        pendingTasks.put("pendingApply", exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .eq(ExchangeApply::getStatus, 0)));
        pendingTasks.put("pendingClaim", claimRecordMapper.selectCount(new LambdaQueryWrapper<ClaimRecord>()
                .eq(ClaimRecord::getPickupStatus, 0)));
        pendingTasks.put("lowStockPoint", pickupPointMapper.selectCount(new LambdaQueryWrapper<PickupPoint>()
                .eq(PickupPoint::getStatus, 1)
                .apply("current_stock < max_capacity * 0.2")));
        dashboard.put("pendingTasks", pendingTasks);
        List<Map<String, Object>> recentItems = new ArrayList<>();
        List<IdleItem> itemList = idleItemMapper.selectList(new LambdaQueryWrapper<IdleItem>()
                .eq(IdleItem::getStatus, 1)
                .orderByDesc(IdleItem::getCreateTime)
                .last("LIMIT 5"));
        for (IdleItem item : itemList) {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("name", item.getItemName());
            itemMap.put("category", item.getCategory());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("username", item.getUsername());
            itemMap.put("createTime", item.getCreateTime());
            recentItems.add(itemMap);
        }
        dashboard.put("recentItems", recentItems);
        List<Map<String, Object>> recentApplies = new ArrayList<>();
        List<ExchangeApply> applyList = exchangeApplyMapper.selectList(new LambdaQueryWrapper<ExchangeApply>()
                .orderByDesc(ExchangeApply::getCreateTime)
                .last("LIMIT 5"));
        for (ExchangeApply apply : applyList) {
            Map<String, Object> applyMap = new LinkedHashMap<>();
            applyMap.put("id", apply.getId());
            applyMap.put("applyNo", apply.getApplyNo());
            applyMap.put("itemName", apply.getItemName());
            applyMap.put("applicantName", apply.getApplicantName());
            applyMap.put("status", apply.getStatus());
            applyMap.put("createTime", apply.getCreateTime());
            recentApplies.add(applyMap);
        }
        dashboard.put("recentApplies", recentApplies);
        return dashboard;
    }

    @Override
    public Map<String, Object> getTrendData(Integer months) {
        if (months == null || months < 1) {
            months = 6;
        }
        Map<String, Object> trend = new LinkedHashMap<>();
        List<String> monthLabels = new ArrayList<>();
        List<Integer> releaseData = new ArrayList<>();
        List<Integer> exchangeData = new ArrayList<>();
        List<Integer> claimData = new ArrayList<>();
        List<Integer> newUserData = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            String monthStr = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthLabels.add(ym.getMonthValue() + "月");
            MonthlyStatistics ms = monthlyStatisticsMapper.selectOne(new LambdaQueryWrapper<MonthlyStatistics>()
                    .eq(MonthlyStatistics::getStatisticsMonth, monthStr));
            if (ms != null) {
                releaseData.add(ms.getReleaseItemCount());
                exchangeData.add(ms.getExchangeSuccessCount());
                claimData.add(ms.getClaimCount());
                newUserData.add(ms.getNewUserCount());
            } else {
                releaseData.add(0);
                exchangeData.add(0);
                claimData.add(0);
                newUserData.add(0);
            }
        }
        trend.put("labels", monthLabels);
        trend.put("releaseData", releaseData);
        trend.put("exchangeData", exchangeData);
        trend.put("claimData", claimData);
        trend.put("newUserData", newUserData);
        Map<String, Object> categoryStats = new LinkedHashMap<>();
        List<IdleItem> allItems = idleItemMapper.selectList(new LambdaQueryWrapper<IdleItem>()
                .select(IdleItem::getCategory, IdleItem::getId));
        Map<String, Integer> categoryCount = new HashMap<>();
        for (IdleItem item : allItems) {
            categoryCount.merge(item.getCategory(), 1, Integer::sum);
        }
        List<Map<String, Object>> categoryList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            Map<String, Object> catMap = new LinkedHashMap<>();
            catMap.put("name", entry.getKey());
            catMap.put("value", entry.getValue());
            categoryList.add(catMap);
        }
        categoryList.sort((a, b) -> ((Integer) b.get("value")).compareTo((Integer) a.get("value")));
        trend.put("categoryData", categoryList);
        Map<String, Object> creditDist = new LinkedHashMap<>();
        String[] levels = {"优秀", "良好", "一般", "较差", "很差"};
        for (String level : levels) {
            creditDist.put(level, userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getCreditLevel, level)
                    .eq(User::getStatus, 1)));
        }
        trend.put("creditDistribution", creditDist);
        return trend;
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        List<IdleItem> allItems = idleItemMapper.selectList(new LambdaQueryWrapper<IdleItem>()
                .select(IdleItem::getCategory, IdleItem::getId));
        Map<String, Integer> categoryCount = new HashMap<>();
        for (IdleItem item : allItems) {
            if (item.getCategory() != null && !item.getCategory().isEmpty()) {
                categoryCount.merge(item.getCategory(), 1, Integer::sum);
            }
        }
        List<Map<String, Object>> categoryList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            Map<String, Object> catMap = new LinkedHashMap<>();
            catMap.put("name", entry.getKey());
            catMap.put("count", entry.getValue());
            categoryList.add(catMap);
        }
        categoryList.sort((a, b) -> ((Integer) b.get("count")).compareTo((Integer) a.get("count")));
        return categoryList;
    }

    @Override
    public Map<String, Object> getCreditDistribution() {
        Map<String, Object> creditDist = new LinkedHashMap<>();
        creditDist.put("excellent", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getCreditLevel, "优秀").eq(User::getStatus, 1)));
        creditDist.put("good", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getCreditLevel, "良好").eq(User::getStatus, 1)));
        creditDist.put("average", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getCreditLevel, "一般").eq(User::getStatus, 1)));
        creditDist.put("poor", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getCreditLevel, "较差").eq(User::getStatus, 1)));
        creditDist.put("veryPoor", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getCreditLevel, "很差").eq(User::getStatus, 1)));
        return creditDist;
    }
}
