package com.community.idle.service;

import com.community.idle.common.PageQuery;
import com.community.idle.common.PageResult;
import com.community.idle.dto.ClaimRecordDTO;
import com.community.idle.entity.ClaimRecord;

public interface ClaimRecordService {
    ClaimRecord createClaim(ClaimRecordDTO dto);
    void confirmPickup(Long id, String pickupCode, String operatorName);
    void cancelClaim(Long id);
    PageResult<ClaimRecord> page(PageQuery query, Integer pickupStatus, String keyword);
    PageResult<ClaimRecord> myClaim(PageQuery query, Integer pickupStatus);
    ClaimRecord detail(Long id);
}
