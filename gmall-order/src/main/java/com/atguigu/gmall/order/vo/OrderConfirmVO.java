package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVO {

    private String orderToken; // 防止重复提交

    private List<MemberReceiveAddressEntity> addressas; // 收获地址列表

    private List<OrderItemVO> items; // 订单详情信息

    private Integer bounds; // 积分信息
}
