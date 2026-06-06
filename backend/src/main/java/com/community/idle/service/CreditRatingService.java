package com.community.idle.service;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.entity.CreditRating;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CreditRatingService {
    void addCredit(Long userId, double value, String reason, Long relatedId, String relatedType);
    void subtractCredit(Long userId, double value, String reason, Long relatedId, String relatedType);
    PageResult<CreditRating> page(PageQuery query, Long userId, String changeType);
    PageResult<CreditRating> myCreditRecord(PageQuery query);
    List<CreditRating> getUserCreditHistory(Long userId);
    Map<String, Object> getCreditLevelConfig();
    void calculateCreditLevel(Long userId);
}
