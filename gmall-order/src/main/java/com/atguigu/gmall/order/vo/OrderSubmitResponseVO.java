package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderEntity;
import lombok.Data;

@Data
public class OrderSubmitResponseVO {

    private Integer code; // 1-重复提交， 2-价格校验失败， 3-库存不足  4- 订单创建异常  5.。。。
    private String msg; // 提示信息
    private String orderToken; // 订单token
    private OrderEntity orderEntity;
}
