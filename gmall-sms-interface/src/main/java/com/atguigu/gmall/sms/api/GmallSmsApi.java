package com.atguigu.gmall.sms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.vo.SaleVO;
import com.atguigu.gmall.sms.vo.SkuSaleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallSmsApi {

    @PostMapping("sms/skubounds/skusale/save")
    public Resp<Object> saveSale(@RequestBody SkuSaleVO skuSaleVO);

    @GetMapping("sms/skuladder/{skuId}")
    public Resp<List<SaleVO>> querySaleVObySkuId(@PathVariable("skuId")Long skuId);
}
