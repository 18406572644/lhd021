package com.community.idle.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.community.idle.common.*;
import com.community.idle.common.annotation.DataScope;
import com.community.idle.entity.IdleItem;
import com.community.idle.entity.PickupPoint;
import com.community.idle.mapper.IdleItemMapper;
import com.community.idle.mapper.PickupPointMapper;
import com.community.idle.service.PickupPointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class PickupPointServiceImpl implements PickupPointService {

    private final PickupPointMapper pickupPointMapper;
    private final IdleItemMapper idleItemMapper;

    public PickupPointServiceImpl(PickupPointMapper pickupPointMapper, IdleItemMapper idleItemMapper) {
        this.pickupPointMapper = pickupPointMapper;
        this.idleItemMapper = idleItemMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(PickupPoint pickupPoint) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        pickupPoint.setStatus(1);
        pickupPoint.setCurrentStock(0);
        pickupPointMapper.insert(pickupPoint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PickupPoint pickupPoint) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        PickupPoint exist = pickupPointMapper.selectById(pickupPoint.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.PICKUP_NOT_EXIST);
        }
        if (pickupPoint.getMaxCapacity() != null && pickupPoint.getMaxCapacity() < exist.getCurrentStock()) {
            throw new BusinessException("最大容量不能小于当前库存");
        }
        pickupPointMapper.updateById(pickupPoint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        PickupPoint exist = pickupPointMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.PICKUP_NOT_EXIST);
        }
        Long count = idleItemMapper.selectCount(new LambdaQueryWrapper<IdleItem>()
                .eq(IdleItem::getPickupPointId, id)
                .in(IdleItem::getStatus, Arrays.asList(1, 2)));
        if (count > 0) {
            throw new BusinessException(ResultCode.PICKUP_IN_USE);
        }
        pickupPointMapper.deleteById(id);
    }

    @Override
    public PickupPoint detail(Long id) {
        PickupPoint pickupPoint = pickupPointMapper.selectById(id);
        if (pickupPoint == null) {
            throw new BusinessException(ResultCode.PICKUP_NOT_EXIST);
        }
        return EntityConverter.convertPickupPoint(pickupPoint);
    }

    @Override
    @DataScope(businessType = "PICKUP_POINT", tableAlias = "", columnName = "id")
    public PageResult<PickupPoint> page(PageQuery query, String keyword, Integer status) {
        LambdaQueryWrapper<PickupPoint> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(PickupPoint::getPointName, keyword)
                    .or().like(PickupPoint::getAddress, keyword)
                    .or().like(PickupPoint::getContactPerson, keyword));
        }
        if (status != null) {
            wrapper.eq(PickupPoint::getStatus, status);
        }
        IPage<PickupPoint> page = pickupPointMapper.selectPage(
                query.buildPage(Arrays.asList(OrderItem.desc("create_time"))), wrapper);
        PageResult<PickupPoint> result = PageResult.of(page);
        result.setList(EntityConverter.convertPickupPointList(result.getList()));
        return result;
    }

    @Override
    public List<PickupPoint> listAll() {
        List<PickupPoint> list = pickupPointMapper.selectList(new LambdaQueryWrapper<PickupPoint>()
                .eq(PickupPoint::getStatus, 1)
                .orderByAsc(PickupPoint::getPointName));
        return EntityConverter.convertPickupPointList(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        PickupPoint pickupPoint = pickupPointMapper.selectById(id);
        if (pickupPoint == null) {
            throw new BusinessException(ResultCode.PICKUP_NOT_EXIST);
        }
        pickupPoint.setStatus(1);
        pickupPointMapper.updateById(pickupPoint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        PickupPoint pickupPoint = pickupPointMapper.selectById(id);
        if (pickupPoint == null) {
            throw new BusinessException(ResultCode.PICKUP_NOT_EXIST);
        }
        Long count = idleItemMapper.selectCount(new LambdaQueryWrapper<IdleItem>()
                .eq(IdleItem::getPickupPointId, id)
                .in(IdleItem::getStatus, Arrays.asList(1, 2)));
        if (count > 0) {
            throw new BusinessException("该自提点仍有上架物品，无法停用");
        }
        pickupPoint.setStatus(0);
        pickupPointMapper.updateById(pickupPoint);
    }
}
