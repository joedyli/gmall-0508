package com.atguigu.gmall.oms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.vo.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallOmsApi {

    @PostMapping("oms/order/{userId}")
    public Resp<OrderEntity> saveOrder(@RequestBody OrderSubmitVO submitVO, @PathVariable("userId")Long userId);
}
