package com.community.idle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.community.idle.common.*;
import com.community.idle.entity.CreditRating;
import com.community.idle.entity.User;
import com.community.idle.mapper.CreditRatingMapper;
import com.community.idle.mapper.UserMapper;
import com.community.idle.service.CreditRatingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CreditRatingServiceImpl implements CreditRatingService {

    private final CreditRatingMapper creditRatingMapper;
    private final UserMapper userMapper;

    public CreditRatingServiceImpl(CreditRatingMapper creditRatingMapper, UserMapper userMapper) {
        this.creditRatingMapper = creditRatingMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCredit(Long userId, double value, String reason, Long relatedId, String relatedType) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        BigDecimal changeValue = new BigDecimal(String.valueOf(value));
        BigDecimal newScore = user.getCreditScore().add(changeValue);
        String newLevel = calculateLevel(newScore);
        CreditRating rating = new CreditRating();
        rating.setUserId(userId);
        rating.setUsername(user.getUsername());
        rating.setScore(newScore);
        rating.setLevel(newLevel);
        rating.setChangeType("ADD");
        rating.setChangeValue(changeValue);
        rating.setChangeReason(reason);
        rating.setRelatedId(relatedId);
        rating.setRelatedType(relatedType);
        rating.setOperatorId(UserContext.getUserId());
        rating.setOperatorName(UserContext.getUsername());
        creditRatingMapper.insert(rating);
        user.setCreditScore(newScore);
        user.setCreditLevel(newLevel);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subtractCredit(Long userId, double value, String reason, Long relatedId, String relatedType) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        BigDecimal changeValue = new BigDecimal(String.valueOf(value));
        BigDecimal newScore = user.getCreditScore().subtract(changeValue);
        if (newScore.compareTo(BigDecimal.ZERO) < 0) {
            newScore = BigDecimal.ZERO;
        }
        String newLevel = calculateLevel(newScore);
        CreditRating rating = new CreditRating();
        rating.setUserId(userId);
        rating.setUsername(user.getUsername());
        rating.setScore(newScore);
        rating.setLevel(newLevel);
        rating.setChangeType("SUBTRACT");
        rating.setChangeValue(changeValue);
        rating.setChangeReason(reason);
        rating.setRelatedId(relatedId);
        rating.setRelatedType(relatedType);
        rating.setOperatorId(UserContext.getUserId());
        rating.setOperatorName(UserContext.getUsername());
        creditRatingMapper.insert(rating);
        user.setCreditScore(newScore);
        user.setCreditLevel(newLevel);
        userMapper.updateById(user);
        if (newScore.compareTo(new BigDecimal("60")) < 0) {
            user.setStatus(0);
            userMapper.updateById(user);
        }
    }

    @Override
    public PageResult<CreditRating> page(PageQuery query, Long userId, String changeType) {
        LambdaQueryWrapper<CreditRating> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(CreditRating::getUserId, userId);
        }
        if (changeType != null && !changeType.isEmpty()) {
            wrapper.eq(CreditRating::getChangeType, changeType);
        }
        IPage<CreditRating> page = creditRatingMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        PageResult<CreditRating> result = PageResult.of(page);
        result.setList(EntityConverter.convertCreditRatingList(result.getList()));
        return result;
    }

    @Override
    public PageResult<CreditRating> myCreditRecord(PageQuery query) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<CreditRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditRating::getUserId, userId);
        IPage<CreditRating> page = creditRatingMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        PageResult<CreditRating> result = PageResult.of(page);
        result.setList(EntityConverter.convertCreditRatingList(result.getList()));
        return result;
    }

    @Override
    public List<CreditRating> getUserCreditHistory(Long userId) {
        List<CreditRating> list = creditRatingMapper.selectList(new LambdaQueryWrapper<CreditRating>()
                .eq(CreditRating::getUserId, userId)
                .orderByDesc(CreditRating::getCreateTime));
        return EntityConverter.convertCreditRatingList(list);
    }

    @Override
    public Map<String, Object> getCreditLevelConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("优秀", new BigDecimal("100"));
        config.put("良好", new BigDecimal("80"));
        config.put("一般", new BigDecimal("60"));
        config.put("较差", new BigDecimal("40"));
        config.put("很差", BigDecimal.ZERO);
        Map<String, Object> result = new HashMap<>();
        result.put("levels", config);
        result.put("minCredit", new BigDecimal("60"));
        result.put("publishMinCredit", new BigDecimal("70"));
        return result;
    }

    @Override
    public void calculateCreditLevel(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            String level = calculateLevel(user.getCreditScore());
            if (!level.equals(user.getCreditLevel())) {
                user.setCreditLevel(level);
                userMapper.updateById(user);
            }
        }
    }

    private String calculateLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("100")) >= 0) {
            return "优秀";
        } else if (score.compareTo(new BigDecimal("80")) >= 0) {
            return "良好";
        } else if (score.compareTo(new BigDecimal("60")) >= 0) {
            return "一般";
        } else if (score.compareTo(new BigDecimal("40")) >= 0) {
            return "较差";
        } else {
            return "很差";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustCredit(Long userId, Integer changeScore, String changeType, String reason, Long operatorId, String operatorName) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        BigDecimal changeValue = new BigDecimal(String.valueOf(Math.abs(changeScore)));
        BigDecimal newScore;
        String actualChangeType;
        
        if (changeScore >= 0) {
            newScore = user.getCreditScore().add(changeValue);
            actualChangeType = "ADD";
        } else {
            newScore = user.getCreditScore().subtract(changeValue);
            if (newScore.compareTo(BigDecimal.ZERO) < 0) {
                newScore = BigDecimal.ZERO;
            }
            actualChangeType = "SUBTRACT";
        }
        
        String newLevel = calculateLevel(newScore);
        CreditRating rating = new CreditRating();
        rating.setUserId(userId);
        rating.setUsername(user.getUsername());
        rating.setScore(newScore);
        rating.setLevel(newLevel);
        rating.setChangeType(changeType != null ? changeType : actualChangeType);
        rating.setChangeValue(changeValue);
        rating.setChangeReason(reason);
        rating.setOperatorId(operatorId != null ? operatorId : UserContext.getUserId());
        rating.setOperatorName(operatorName != null ? operatorName : UserContext.getUsername());
        creditRatingMapper.insert(rating);
        
        user.setCreditScore(newScore);
        user.setCreditLevel(newLevel);
        userMapper.updateById(user);
        
        if (newScore.compareTo(new BigDecimal("60")) < 0) {
            user.setStatus(0);
            userMapper.updateById(user);
        }
    }
}
