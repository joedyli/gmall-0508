package com.atguigu.gmall.wms.entity;

import lombok.Data;

@Data
public class SkuLockVO {

    private Long skuId;

    private Integer num;

    private Long wareSkuId; // 锁定库存的id

    private String orderToken;
}
