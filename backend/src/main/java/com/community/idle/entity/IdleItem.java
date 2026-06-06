package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("idle_item")
public class IdleItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

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

    private Integer status;

    private Integer viewCount;

    private Integer exchangeCount;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime offlineTime;

    @TableLogic
    private Integer deleted;
}
