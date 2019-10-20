package com.atguigu.gmall.oms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVO {

    private String orderToken;

    private Long addressId; // 用户选择的收获地址信息

    private Integer payType; // 支付方式

    private String delivery_company; // 配送方式，快递公司

    private List<OrderItemVO> items; // 送货清单

    private Integer bounds; // 消费积分

    private BigDecimal totalPrice; // 总价
}
