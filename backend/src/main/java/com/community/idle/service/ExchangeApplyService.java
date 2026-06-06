package com.community.idle.service;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.dto.ExchangeApplyDTO;
import com.community.idle.entity.ExchangeApply;

import java.util.Map;

public interface ExchangeApplyService {
    void apply(ExchangeApplyDTO dto);
    void approve(Long id, String remark);
    void reject(Long id, String rejectReason);
    void cancel(Long id);
    void complete(Long id);
    PageResult<ExchangeApply> page(PageQuery query, Integer status, String keyword);
    PageResult<ExchangeApply> myApply(PageQuery query, Integer status);
    PageResult<ExchangeApply> myReceive(PageQuery query, Integer status);
    ExchangeApply detail(Long id);
    Map<String, Object> getStatusCount();
}
