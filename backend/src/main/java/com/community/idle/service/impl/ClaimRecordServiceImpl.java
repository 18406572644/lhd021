package com.community.idle.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.community.idle.common.*;
import com.community.idle.dto.ClaimRecordDTO;
import com.community.idle.entity.ClaimRecord;
import com.community.idle.entity.IdleItem;
import com.community.idle.entity.User;
import com.community.idle.mapper.ClaimRecordMapper;
import com.community.idle.mapper.IdleItemMapper;
import com.community.idle.mapper.UserMapper;
import com.community.idle.service.ClaimRecordService;
import com.community.idle.service.CreditRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ClaimRecordServiceImpl implements ClaimRecordService {

    private final ClaimRecordMapper claimRecordMapper;
    private final IdleItemMapper idleItemMapper;
    private final UserMapper userMapper;
    private final CreditRatingService creditRatingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClaimRecord createClaim(ClaimRecordDTO dto) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user.getCreditScore().compareTo(new java.math.BigDecimal("60")) < 0) {
            throw new BusinessException("信用分不足60分，无法领用物品");
        }
        IdleItem item = idleItemMapper.selectById(dto.getItemId());
        if (item == null) {
            throw new BusinessException(ResultCode.ITEM_NOT_EXIST);
        }
        if (item.getStatus() != 1) {
            throw new BusinessException(ResultCode.ITEM_STATUS_ERROR);
        }
        if (item.getUserId().equals(userId)) {
            throw new BusinessException("不能领用自己发布的物品");
        }
        if (dto.getClaimQuantity() > item.getQuantity()) {
            throw new BusinessException("领用数量超过物品可用数量");
        }
        String recordNo = "CR" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") +
                String.format("%04d", (int) (Math.random() * 10000));
        String pickupCode = "A" + RandomUtil.randomNumbers(4);
        ClaimRecord record = new ClaimRecord();
        record.setRecordNo(recordNo);
        record.setItemId(dto.getItemId());
        record.setItemName(item.getItemName());
        record.setItemImages(item.getItemImages());
        record.setItemOwnerId(item.getUserId());
        record.setItemOwnerName(item.getUsername());
        record.setClaimUserId(userId);
        record.setClaimUserName(UserContext.getUsername());
        record.setClaimUserPhone(dto.getClaimUserPhone());
        record.setClaimQuantity(dto.getClaimQuantity());
        record.setExchangeApplyId(dto.getExchangeApplyId());
        record.setPickupPointId(item.getPickupPointId());
        record.setPickupPointName(item.getPickupPointName());
        record.setClaimTime(LocalDateTime.now());
        record.setPickupStatus(0);
        record.setPickupCode(pickupCode);
        record.setRemark(dto.getRemark());
        claimRecordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPickup(Long id, String pickupCode, String operatorName) {
        ClaimRecord record = claimRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("领用记录不存在");
        }
        if (record.getPickupStatus() != 0) {
            throw new BusinessException("领用状态错误");
        }
        if (!pickupCode.equals(record.getPickupCode())) {
            throw new BusinessException("取件码错误");
        }
        record.setPickupStatus(1);
        record.setPickupTime(LocalDateTime.now());
        record.setOperatorName(operatorName);
        claimRecordMapper.updateById(record);
        IdleItem item = idleItemMapper.selectById(record.getItemId());
        if (item != null) {
            int remainQuantity = item.getQuantity() - record.getClaimQuantity();
            item.setExchangeCount(item.getExchangeCount() + 1);
            if (remainQuantity <= 0) {
                item.setStatus(3);
                item.setQuantity(0);
            } else {
                item.setQuantity(remainQuantity);
            }
            idleItemMapper.updateById(item);
            com.community.idle.entity.PickupPoint pickupPoint =
                    new com.community.idle.entity.PickupPoint();
            pickupPoint.setId(item.getPickupPointId());
            pickupPoint.setCurrentStock(Math.max(0,
                    pickupPoint.getCurrentStock() == null ? 0 : pickupPoint.getCurrentStock() - record.getClaimQuantity()));
        }
        User claimUser = userMapper.selectById(record.getClaimUserId());
        claimUser.setExchangeCount(claimUser.getExchangeCount() + 1);
        userMapper.updateById(claimUser);
        User ownerUser = userMapper.selectById(record.getItemOwnerId());
        ownerUser.setExchangeCount(ownerUser.getExchangeCount() + 1);
        userMapper.updateById(ownerUser);
        creditRatingService.addCredit(record.getClaimUserId(), 2, "完成物品领用",
                record.getId(), "CLAIM_RECORD");
        creditRatingService.addCredit(record.getItemOwnerId(), 2, "物品被成功领用",
                record.getId(), "CLAIM_RECORD");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelClaim(Long id) {
        ClaimRecord record = claimRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("领用记录不存在");
        }
        if (!record.getClaimUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (record.getPickupStatus() != 0) {
            throw new BusinessException("只能取消待领取的记录");
        }
        record.setPickupStatus(2);
        claimRecordMapper.updateById(record);
        creditRatingService.subtractCredit(record.getClaimUserId(), 2, "取消物品领用",
                record.getId(), "CLAIM_RECORD");
    }

    @Override
    public PageResult<ClaimRecord> page(PageQuery query, Integer pickupStatus, String keyword) {
        LambdaQueryWrapper<ClaimRecord> wrapper = new LambdaQueryWrapper<>();
        if (pickupStatus != null) {
            wrapper.eq(ClaimRecord::getPickupStatus, pickupStatus);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(ClaimRecord::getRecordNo, keyword)
                    .or().like(ClaimRecord::getItemName, keyword)
                    .or().like(ClaimRecord::getClaimUserName, keyword));
        }
        IPage<ClaimRecord> page = claimRecordMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        return PageResult.of(page);
    }

    @Override
    public PageResult<ClaimRecord> myClaim(PageQuery query, Integer pickupStatus) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<ClaimRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ClaimRecord::getClaimUserId, userId)
                .or().eq(ClaimRecord::getItemOwnerId, userId));
        if (pickupStatus != null) {
            wrapper.eq(ClaimRecord::getPickupStatus, pickupStatus);
        }
        IPage<ClaimRecord> page = claimRecordMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        return PageResult.of(page);
    }

    @Override
    public ClaimRecord detail(Long id) {
        ClaimRecord record = claimRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("领用记录不存在");
        }
        return record;
    }
}
