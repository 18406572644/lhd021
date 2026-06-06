package com.community.idle.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.community.idle.common.*;
import com.community.idle.dto.ExchangeApplyDTO;
import com.community.idle.entity.ExchangeApply;
import com.community.idle.entity.IdleItem;
import com.community.idle.entity.User;
import com.community.idle.mapper.ExchangeApplyMapper;
import com.community.idle.mapper.IdleItemMapper;
import com.community.idle.mapper.UserMapper;
import com.community.idle.service.CreditRatingService;
import com.community.idle.service.ExchangeApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeApplyServiceImpl implements ExchangeApplyService {

    private final ExchangeApplyMapper exchangeApplyMapper;
    private final IdleItemMapper idleItemMapper;
    private final UserMapper userMapper;
    private final CreditRatingService creditRatingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(ExchangeApplyDTO dto) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user.getCreditScore().compareTo(new java.math.BigDecimal("60")) < 0) {
            throw new BusinessException("信用分不足60分，无法申请互换");
        }
        IdleItem item = idleItemMapper.selectById(dto.getItemId());
        if (item == null) {
            throw new BusinessException(ResultCode.ITEM_NOT_EXIST);
        }
        if (item.getStatus() != 1) {
            throw new BusinessException(ResultCode.ITEM_STATUS_ERROR);
        }
        if (item.getUserId().equals(userId)) {
            throw new BusinessException("不能申请自己发布的物品");
        }
        if (dto.getApplyQuantity() > item.getQuantity()) {
            throw new BusinessException("申请数量超过物品可用数量");
        }
        ExchangeApply existApply = exchangeApplyMapper.selectOne(new LambdaQueryWrapper<ExchangeApply>()
                .eq(ExchangeApply::getItemId, dto.getItemId())
                .eq(ExchangeApply::getApplicantId, userId)
                .in(ExchangeApply::getStatus, Arrays.asList(0, 1, 2)));
        if (existApply != null) {
            throw new BusinessException("您已申请过该物品，请等待审核或取消后重新申请");
        }
        String applyNo = "EX" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") +
                String.format("%04d", (int) (Math.random() * 10000));
        ExchangeApply apply = new ExchangeApply();
        apply.setApplyNo(applyNo);
        apply.setItemId(dto.getItemId());
        apply.setItemName(item.getItemName());
        apply.setItemImages(item.getItemImages());
        apply.setItemOwnerId(item.getUserId());
        apply.setItemOwnerName(item.getUsername());
        apply.setApplicantId(userId);
        apply.setApplicantName(UserContext.getUsername());
        apply.setApplicantPhone(dto.getApplicantPhone());
        apply.setApplyQuantity(dto.getApplyQuantity());
        apply.setApplyReason(dto.getApplyReason());
        apply.setExchangeItemDesc(dto.getExchangeItemDesc());
        apply.setStatus(0);
        exchangeApplyMapper.insert(apply);
        item.setStatus(2);
        idleItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String remark) {
        ExchangeApply apply = exchangeApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(ResultCode.EXCHANGE_NOT_EXIST);
        }
        if (apply.getStatus() != 0) {
            throw new BusinessException(ResultCode.EXCHANGE_ALREADY_PROCESSED);
        }
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        User applicant = userMapper.selectById(apply.getApplicantId());
        if (applicant.getCreditScore().compareTo(new java.math.BigDecimal("60")) < 0) {
            throw new BusinessException("申请人信用分不足，审核不通过");
        }
        apply.setStatus(1);
        apply.setAuditUserId(UserContext.getUserId());
        apply.setAuditUserName(UserContext.getUsername());
        apply.setAuditTime(LocalDateTime.now());
        apply.setAuditRemark(remark);
        exchangeApplyMapper.updateById(apply);
        creditRatingService.addCredit(applicant.getId(), 2, "互换申请审核通过",
                apply.getId(), "EXCHANGE_APPLY");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String rejectReason) {
        ExchangeApply apply = exchangeApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(ResultCode.EXCHANGE_NOT_EXIST);
        }
        if (apply.getStatus() != 0) {
            throw new BusinessException(ResultCode.EXCHANGE_ALREADY_PROCESSED);
        }
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        apply.setStatus(2);
        apply.setAuditUserId(UserContext.getUserId());
        apply.setAuditUserName(UserContext.getUsername());
        apply.setAuditTime(LocalDateTime.now());
        apply.setRejectReason(rejectReason);
        exchangeApplyMapper.updateById(apply);
        IdleItem item = idleItemMapper.selectById(apply.getItemId());
        if (item != null && item.getStatus() == 2) {
            item.setStatus(1);
            idleItemMapper.updateById(item);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        ExchangeApply apply = exchangeApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(ResultCode.EXCHANGE_NOT_EXIST);
        }
        if (!apply.getApplicantId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (apply.getStatus() != 0 && apply.getStatus() != 1) {
            throw new BusinessException(ResultCode.EXCHANGE_STATUS_ERROR);
        }
        apply.setStatus(4);
        exchangeApplyMapper.updateById(apply);
        IdleItem item = idleItemMapper.selectById(apply.getItemId());
        if (item != null && item.getStatus() == 2) {
            item.setStatus(1);
            idleItemMapper.updateById(item);
        }
        creditRatingService.subtractCredit(apply.getApplicantId(), 3, "取消互换申请",
                apply.getId(), "EXCHANGE_APPLY");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        ExchangeApply apply = exchangeApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(ResultCode.EXCHANGE_NOT_EXIST);
        }
        if (apply.getStatus() != 1) {
            throw new BusinessException(ResultCode.EXCHANGE_STATUS_ERROR);
        }
        if (!apply.getItemOwnerId().equals(UserContext.getUserId()) && !UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        apply.setStatus(3);
        exchangeApplyMapper.updateById(apply);
        IdleItem item = idleItemMapper.selectById(apply.getItemId());
        if (item != null) {
            int remainQuantity = item.getQuantity() - apply.getApplyQuantity();
            item.setExchangeCount(item.getExchangeCount() + 1);
            if (remainQuantity <= 0) {
                item.setStatus(3);
                item.setQuantity(0);
            } else {
                item.setQuantity(remainQuantity);
                item.setStatus(1);
            }
            idleItemMapper.updateById(item);
        }
        User applicant = userMapper.selectById(apply.getApplicantId());
        applicant.setExchangeCount(applicant.getExchangeCount() + 1);
        userMapper.updateById(applicant);
        User owner = userMapper.selectById(apply.getItemOwnerId());
        owner.setExchangeCount(owner.getExchangeCount() + 1);
        userMapper.updateById(owner);
        creditRatingService.addCredit(apply.getApplicantId(), 3, "完成互换",
                apply.getId(), "EXCHANGE_APPLY");
        creditRatingService.addCredit(apply.getItemOwnerId(), 3, "物品互换成功",
                apply.getId(), "EXCHANGE_APPLY");
    }

    @Override
    public PageResult<ExchangeApply> page(PageQuery query, Integer status, String keyword) {
        LambdaQueryWrapper<ExchangeApply> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ExchangeApply::getStatus, status);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(ExchangeApply::getApplyNo, keyword)
                    .or().like(ExchangeApply::getItemName, keyword)
                    .or().like(ExchangeApply::getApplicantName, keyword));
        }
        IPage<ExchangeApply> page = exchangeApplyMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        return PageResult.of(page);
    }

    @Override
    public PageResult<ExchangeApply> myApply(PageQuery query, Integer status) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<ExchangeApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeApply::getApplicantId, userId);
        if (status != null) {
            wrapper.eq(ExchangeApply::getStatus, status);
        }
        IPage<ExchangeApply> page = exchangeApplyMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        return PageResult.of(page);
    }

    @Override
    public PageResult<ExchangeApply> myReceive(PageQuery query, Integer status) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<ExchangeApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeApply::getItemOwnerId, userId);
        if (status != null) {
            wrapper.eq(ExchangeApply::getStatus, status);
        }
        IPage<ExchangeApply> page = exchangeApplyMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        return PageResult.of(page);
    }

    @Override
    public ExchangeApply detail(Long id) {
        ExchangeApply apply = exchangeApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(ResultCode.EXCHANGE_NOT_EXIST);
        }
        return apply;
    }

    @Override
    public Map<String, Object> getStatusCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("pending", exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .eq(ExchangeApply::getStatus, 0)));
        result.put("approved", exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .eq(ExchangeApply::getStatus, 1)));
        result.put("rejected", exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .eq(ExchangeApply::getStatus, 2)));
        result.put("completed", exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .eq(ExchangeApply::getStatus, 3)));
        result.put("cancelled", exchangeApplyMapper.selectCount(new LambdaQueryWrapper<ExchangeApply>()
                .eq(ExchangeApply::getStatus, 4)));
        return result;
    }
}
