package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("claim_record")
public class ClaimRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String recordNo;

    private Long itemId;

    private String itemName;

    private String itemImages;

    private Long itemOwnerId;

    private String itemOwnerName;

    private Long claimUserId;

    private String claimUserName;

    private String claimUserPhone;

    private Integer claimQuantity;

    private Long exchangeApplyId;

    private Long pickupPointId;

    private String pickupPointName;

    private LocalDateTime claimTime;

    private LocalDateTime pickupTime;

    private Integer pickupStatus;

    private String pickupCode;

    private String operatorName;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
