package com.community.idle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("pickup_point")
public class PickupPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String pointName;

    private String address;

    private String contactPerson;

    private String contactPhone;

    private String businessHours;

    private String pointDesc;

    private Double longitude;

    private Double latitude;

    private Integer maxCapacity;

    private Integer currentStock;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
