package com.community.idle.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.community.idle.common.*;
import com.community.idle.common.annotation.DataScope;
import com.community.idle.entity.IdleItem;
import com.community.idle.entity.ItemArchive;
import com.community.idle.entity.PickupPoint;
import com.community.idle.mapper.IdleItemMapper;
import com.community.idle.mapper.ItemArchiveMapper;
import com.community.idle.mapper.PickupPointMapper;
import com.community.idle.service.ItemArchiveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class ItemArchiveServiceImpl implements ItemArchiveService {

    private final ItemArchiveMapper itemArchiveMapper;
    private final IdleItemMapper idleItemMapper;
    private final PickupPointMapper pickupPointMapper;

    public ItemArchiveServiceImpl(ItemArchiveMapper itemArchiveMapper, IdleItemMapper idleItemMapper, PickupPointMapper pickupPointMapper) {
        this.itemArchiveMapper = itemArchiveMapper;
        this.idleItemMapper = idleItemMapper;
        this.pickupPointMapper = pickupPointMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archive(Long itemId, String reason) {
        IdleItem item = idleItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ResultCode.ITEM_NOT_EXIST);
        }
        if (!item.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (item.getStatus() == 3) {
            throw new BusinessException("物品已归档，无需重复操作");
        }
        if (item.getStatus() == 2) {
            throw new BusinessException("物品正在互换流程中，无法归档");
        }
        long onlineDays = ChronoUnit.DAYS.between(item.getCreateTime(), LocalDateTime.now());
        String archiveNo = "AR" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") +
                String.format("%04d", (int) (Math.random() * 10000));
        ItemArchive archive = new ItemArchive();
        archive.setArchiveNo(archiveNo);
        archive.setItemId(item.getId());
        archive.setItemName(item.getItemName());
        archive.setItemDesc(item.getItemDesc());
        archive.setCategory(item.getCategory());
        archive.setItemImages(item.getItemImages());
        archive.setQuantity(item.getQuantity());
        archive.setOriginalValue(item.getOriginalValue());
        archive.setConditionLevel(item.getConditionLevel());
        archive.setPickupPointId(item.getPickupPointId());
        archive.setPickupPointName(item.getPickupPointName());
        archive.setUserId(item.getUserId());
        archive.setUsername(item.getUsername());
        archive.setOnlineDays((int) onlineDays);
        archive.setViewCount(item.getViewCount());
        archive.setExchangeCount(item.getExchangeCount());
        archive.setArchiveReason(reason);
        archive.setArchiveType(UserContext.isAdmin() ? "MANUAL" : "MANUAL");
        archive.setOperatorId(UserContext.getUserId());
        archive.setOperatorName(UserContext.getUsername());
        itemArchiveMapper.insert(archive);
        item.setStatus(3);
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
    public void archiveBatch(List<Long> itemIds, String reason) {
        for (Long itemId : itemIds) {
            archive(itemId, reason);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoArchive() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<IdleItem> items = idleItemMapper.selectList(new LambdaQueryWrapper<IdleItem>()
                .eq(IdleItem::getStatus, 1)
                .lt(IdleItem::getUpdateTime, thirtyDaysAgo));
        for (IdleItem item : items) {
            long onlineDays = ChronoUnit.DAYS.between(item.getCreateTime(), LocalDateTime.now());
            String archiveNo = "AR" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") +
                    String.format("%04d", (int) (Math.random() * 10000));
            ItemArchive archive = new ItemArchive();
            archive.setArchiveNo(archiveNo);
            archive.setItemId(item.getId());
            archive.setItemName(item.getItemName());
            archive.setItemDesc(item.getItemDesc());
            archive.setCategory(item.getCategory());
            archive.setItemImages(item.getItemImages());
            archive.setQuantity(item.getQuantity());
            archive.setOriginalValue(item.getOriginalValue());
            archive.setConditionLevel(item.getConditionLevel());
            archive.setPickupPointId(item.getPickupPointId());
            archive.setPickupPointName(item.getPickupPointName());
            archive.setUserId(item.getUserId());
            archive.setUsername(item.getUsername());
            archive.setOnlineDays((int) onlineDays);
            archive.setViewCount(item.getViewCount());
            archive.setExchangeCount(item.getExchangeCount());
            archive.setArchiveReason("系统自动归档：上架超过30天无动静");
            archive.setArchiveType("AUTO");
            archive.setOperatorId(1L);
            archive.setOperatorName("system");
            itemArchiveMapper.insert(archive);
            item.setStatus(3);
            item.setOfflineTime(LocalDateTime.now());
            idleItemMapper.updateById(item);
            PickupPoint pickupPoint = pickupPointMapper.selectById(item.getPickupPointId());
            if (pickupPoint != null) {
                pickupPoint.setCurrentStock(Math.max(0, pickupPoint.getCurrentStock() - item.getQuantity()));
                pickupPointMapper.updateById(pickupPoint);
            }
        }
    }

    @Override
    @DataScope(businessType = "PICKUP_POINT", tableAlias = "", columnName = "pickup_point_id", userScope = true, userColumnName = "user_id")
    public PageResult<ItemArchive> page(PageQuery query, String keyword, String archiveType) {
        LambdaQueryWrapper<ItemArchive> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(ItemArchive::getItemName, keyword)
                    .or().like(ItemArchive::getArchiveNo, keyword)
                    .or().like(ItemArchive::getUsername, keyword));
        }
        if (StrUtil.isNotBlank(archiveType)) {
            wrapper.eq(ItemArchive::getArchiveType, archiveType);
        }
        IPage<ItemArchive> page = itemArchiveMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        PageResult<ItemArchive> result = PageResult.of(page);
        result.setList(EntityConverter.convertItemArchiveList(result.getList()));
        return result;
    }

    @Override
    public ItemArchive detail(Long id) {
        ItemArchive archive = itemArchiveMapper.selectById(id);
        if (archive == null) {
            throw new BusinessException("归档记录不存在");
        }
        return EntityConverter.convertItemArchive(archive);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemArchive restore(Long id) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        ItemArchive archive = itemArchiveMapper.selectById(id);
        if (archive == null) {
            throw new BusinessException("归档记录不存在");
        }
        IdleItem item = idleItemMapper.selectById(archive.getItemId());
        if (item != null && item.getStatus() == 3) {
            PickupPoint pickupPoint = pickupPointMapper.selectById(item.getPickupPointId());
            if (pickupPoint == null || pickupPoint.getStatus() == 0) {
                throw new BusinessException("物品原自提点已停用，无法恢复");
            }
            if (pickupPoint.getCurrentStock() + item.getQuantity() > pickupPoint.getMaxCapacity()) {
                throw new BusinessException("自提点库存不足，无法恢复");
            }
            item.setStatus(1);
            item.setOfflineTime(null);
            idleItemMapper.updateById(item);
            pickupPoint.setCurrentStock(pickupPoint.getCurrentStock() + item.getQuantity());
            pickupPointMapper.updateById(pickupPoint);
            itemArchiveMapper.deleteById(id);
        }
        return EntityConverter.convertItemArchive(archive);
    }
}
