package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("monthly_statistics")
public class MonthlyStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String statisticsMonth;

    private Integer newUserCount;

    private Integer activeUserCount;

    private Integer releaseItemCount;

    private Integer exchangeApplyCount;

    private Integer exchangeSuccessCount;

    private BigDecimal exchangeSuccessRate;

    private Integer claimCount;

    private Integer archiveItemCount;

    private BigDecimal totalValue;

    private Integer creditUpgradeCount;

    private Integer creditDowngradeCount;

    private Integer pickupPointCount;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
