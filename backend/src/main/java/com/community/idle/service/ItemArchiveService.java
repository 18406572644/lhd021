package com.community.idle.service;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.entity.ItemArchive;

import java.util.List;

public interface ItemArchiveService {
    void archive(Long itemId, String reason);
    void archiveBatch(List<Long> itemIds, String reason);
    void autoArchive();
    PageResult<ItemArchive> page(PageQuery query, String keyword, String archiveType);
    ItemArchive detail(Long id);
    ItemArchive restore(Long id);
}
