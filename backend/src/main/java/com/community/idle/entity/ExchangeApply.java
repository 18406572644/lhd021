package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("exchange_apply")
public class ExchangeApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyNo;

    private Long itemId;

    private String itemName;

    private String itemImages;

    private Long itemOwnerId;

    private String itemOwnerName;

    private Long applicantId;

    private String applicantName;

    private String applicantPhone;

    private Integer applyQuantity;

    private String applyReason;

    private String exchangeItemDesc;

    private Integer status;

    private Long auditUserId;

    private String auditUserName;

    private LocalDateTime auditTime;

    private String auditRemark;

    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
