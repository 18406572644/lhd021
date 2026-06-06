package com.community.idle.service;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.entity.PickupPoint;

import java.util.List;

public interface PickupPointService {
    void add(PickupPoint pickupPoint);
    void update(PickupPoint pickupPoint);
    void delete(Long id);
    PickupPoint detail(Long id);
    PageResult<PickupPoint> page(PageQuery query, String keyword, Integer status);
    List<PickupPoint> listAll();
    void enable(Long id);
    void disable(Long id);
}
