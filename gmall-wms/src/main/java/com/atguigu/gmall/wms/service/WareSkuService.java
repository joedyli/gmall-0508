package com.atguigu.gmall.wms.service;

import com.atguigu.gmall.wms.entity.SkuLockVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品库存
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2019-09-21 11:34:08
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageVo queryPage(QueryCondition params);

    String checkAndLockStock(List<SkuLockVO> skuLockVOS);

    void unlockStock(String orderToken);

    void minus(String orderToken);
}

