package com.community.idle.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.community.idle.common.BusinessException;
import com.community.idle.common.EntityConverter;
import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.common.ResultCode;
import com.community.idle.common.UserContext;
import com.community.idle.common.annotation.DataScope;
import com.community.idle.dto.IdleItemDTO;
import com.community.idle.entity.IdleItem;
import com.community.idle.entity.PickupPoint;
import com.community.idle.entity.User;
import com.community.idle.mapper.IdleItemMapper;
import com.community.idle.mapper.PickupPointMapper;
import com.community.idle.mapper.UserMapper;
import com.community.idle.service.IdleItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IdleItemServiceImpl implements IdleItemService {

    private final IdleItemMapper idleItemMapper;
    private final PickupPointMapper pickupPointMapper;
    private final UserMapper userMapper;

    public IdleItemServiceImpl(IdleItemMapper idleItemMapper, PickupPointMapper pickupPointMapper, UserMapper userMapper) {
        this.idleItemMapper = idleItemMapper;
        this.pickupPointMapper = pickupPointMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(IdleItemDTO dto) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user.getCreditScore().compareTo(new java.math.BigDecimal("70")) < 0) {
            throw new BusinessException("信用分不足70分，无法发布物品");
        }
        PickupPoint pickupPoint = pickupPointMapper.selectById(dto.getPickupPointId());
        if (pickupPoint == null || pickupPoint.getStatus() == 0) {
            throw new BusinessException(ResultCode.PICKUP_NOT_EXIST);
        }
        if (pickupPoint.getCurrentStock() >= pickupPoint.getMaxCapacity()) {
            throw new BusinessException("该自提点库存已满，请选择其他自提点");
        }
        IdleItem item = new IdleItem();
        item.setItemName(dto.getItemName());
        item.setItemDesc(dto.getItemDesc());
        item.setCategory(dto.getCategory());
        item.setItemImages(dto.getItemImages());
        item.setQuantity(dto.getQuantity());
        item.setOriginalValue(dto.getOriginalValue());
        item.setConditionLevel(dto.getConditionLevel());
        item.setPickupPointId(dto.getPickupPointId());
        item.setPickupPointName(pickupPoint.getPointName());
        item.setUserId(userId);
        item.setUsername(UserContext.getUsername());
        item.setStatus(1);
        item.setViewCount(0);
        item.setExchangeCount(0);
        item.setRemark(dto.getRemark());
        idleItemMapper.insert(item);
        pickupPoint.setCurrentStock(pickupPoint.getCurrentStock() + dto.getQuantity());
        pickupPointMapper.updateById(pickupPoint);
        user.setReleaseCount(user.getReleaseCount() + 1);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, IdleItemDTO dto) {
        IdleItem item = idleItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.ITEM_NOT_EXIST);
        }
        if (!item.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (item.getStatus() != 1) {
            throw new BusinessException(ResultCode.ITEM_STATUS_ERROR);
        }
        if (!item.getPickupPointId().equals(dto.getPickupPointId())) {
            PickupPoint oldPoint = pickupPointMapper.selectById(item.getPickupPointId());
            PickupPoint newPoint = pickupPointMapper.selectById(dto.getPickupPointId());
            if (newPoint == null || newPoint.getStatus() == 0) {
                throw new BusinessException(ResultCode.PICKUP_NOT_EXIST);
            }
            if (newPoint.getCurrentStock() + dto.getQuantity() > newPoint.getMaxCapacity()) {
                throw new BusinessException("新自提点库存不足");
            }
            oldPoint.setCurrentStock(oldPoint.getCurrentStock() - item.getQuantity());
            newPoint.setCurrentStock(newPoint.getCurrentStock() + dto.getQuantity());
            pickupPointMapper.updateById(oldPoint);
            pickupPointMapper.updateById(newPoint);
            item.setPickupPointId(dto.getPickupPointId());
            item.setPickupPointName(newPoint.getPointName());
        }
        item.setItemName(dto.getItemName());
        item.setItemDesc(dto.getItemDesc());
        item.setCategory(dto.getCategory());
        item.setItemImages(dto.getItemImages());
        item.setQuantity(dto.getQuantity());
        item.setOriginalValue(dto.getOriginalValue());
        item.setConditionLevel(dto.getConditionLevel());
        item.setRemark(dto.getRemark());
        idleItemMapper.updateById(item);
    }

    @Override
    public PageResult<IdleItem> page(PageQuery query, String keyword, String category, Integer status) {
        LambdaQueryWrapper<IdleItem> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(IdleItem::getItemName, keyword)
                    .or().like(IdleItem::getItemDesc, keyword));
        }
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(IdleItem::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(IdleItem::getStatus, status);
        }
        IPage<IdleItem> page = idleItemMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        PageResult<IdleItem> result = PageResult.of(page);
        result.setList(EntityConverter.convertIdleItemList(result.getList()));
        return result;
    }

    @Override
    public IdleItem detail(Long id) {
        IdleItem item = idleItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.ITEM_NOT_EXIST);
        }
        item.setViewCount(item.getViewCount() + 1);
        idleItemMapper.updateById(item);
        return EntityConverter.convertIdleItem(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offline(Long id) {
        IdleItem item = idleItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.ITEM_NOT_EXIST);
        }
        if (!item.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (item.getStatus() == 0 || item.getStatus() == 3) {
            throw new BusinessException(ResultCode.ITEM_STATUS_ERROR);
        }
        if (item.getStatus() == 2) {
            throw new BusinessException("物品正在互换流程中，无法下架");
        }
        item.setStatus(0);
        item.setOfflineTime(LocalDateTime.now());
        idleItemMapper.updateById(item);
        PickupPoint pickupPoint = pickupPointMapper.selectById(item.getPickupPointId());
        if (pickupPoint != null) {
            pickupPoint.setCurrentStock(Math.max(0, pickupPoint.getCurrentStock() - item.getQuantity()));
            pickupPointMapper.updateById(pickupPoint);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineBatch(List<Long> ids) {
        for (Long id : ids) {
            offline(id);
        }
    }

    @Override
    public PageResult<IdleItem> myPublish(PageQuery query, Integer status) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<IdleItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IdleItem::getUserId, userId);
        if (status != null) {
            wrapper.eq(IdleItem::getStatus, status);
        }
        IPage<IdleItem> page = idleItemMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        PageResult<IdleItem> result = PageResult.of(page);
        result.setList(EntityConverter.convertIdleItemList(result.getList()));
        return result;
    }

    @Override
    public List<String> getCategories() {
        List<IdleItem> items = idleItemMapper.selectList(new LambdaQueryWrapper<IdleItem>()
                .select(IdleItem::getCategory)
                .groupBy(IdleItem::getCategory));
        return items.stream().map(IdleItem::getCategory).distinct().collect(Collectors.toList());
    }
}
