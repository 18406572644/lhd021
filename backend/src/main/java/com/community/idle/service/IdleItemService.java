package com.community.idle.service;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.dto.IdleItemDTO;
import com.community.idle.entity.IdleItem;

import java.util.List;

public interface IdleItemService {
    void publish(IdleItemDTO dto);
    void update(Long id, IdleItemDTO dto);
    PageResult<IdleItem> page(PageQuery query, String keyword, String category, Integer status);
    IdleItem detail(Long id);
    void offline(Long id);
    void offlineBatch(List<Long> ids);
    PageResult<IdleItem> myPublish(PageQuery query, Integer status);
    List<String> getCategories();
}
