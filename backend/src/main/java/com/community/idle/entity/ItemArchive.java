package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("item_archive")
public class ItemArchive implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String archiveNo;

    private Long itemId;

    private String itemName;

    private String itemDesc;

    private String category;

    private String itemImages;

    private Integer quantity;

    private BigDecimal originalValue;

    private Integer conditionLevel;

    private Long pickupPointId;

    private String pickupPointName;

    private Long userId;

    private String username;

    private Integer onlineDays;

    private Integer viewCount;

    private Integer exchangeCount;

    private String archiveReason;

    private String archiveType;

    private Long operatorId;

    private String operatorName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
