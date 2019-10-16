package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.vo.SaleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.service.SkuLadderService;


@Service("skuLadderService")
public class SkuLadderServiceImpl extends ServiceImpl<SkuLadderDao, SkuLadderEntity> implements SkuLadderService {

    @Autowired
    private SkuLadderDao skuLadderDao;

    @Autowired
    private SkuBoundsDao skuBoundsDao;

    @Autowired
    private SkuFullReductionDao skuFullReductionDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuLadderEntity> page = this.page(
                new Query<SkuLadderEntity>().getPage(params),
                new QueryWrapper<SkuLadderEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<SaleVO> querySaleVObySkuId(Long skuId) {

        // 根据skuId查询积分
        List<SkuBoundsEntity> skuBoundsEntities = this.skuBoundsDao.selectList(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        // 根据skuId查询阶梯优惠（打折）
        List<SkuLadderEntity> skuLadderEntities = this.skuLadderDao.selectList(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        // 根据skuId查询满减
        List<SkuFullReductionEntity> skuFullReductionEntities = this.skuFullReductionDao.selectList(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));

        List<SaleVO> saleVOS = new ArrayList<>();

        skuBoundsEntities.forEach(skuBoundsEntity -> {
            saleVOS.add(new SaleVO("积分", "赠送" + skuBoundsEntity.getBuyBounds() + "积分"));
        });

        skuLadderEntities.forEach(skuLadderEntity -> {
            saleVOS.add(new SaleVO("打折", "购买" + skuLadderEntity.getFullCount() + "件，打" + skuLadderEntity.getDiscount().divide(new BigDecimal(10)) + "折"));
        });

        skuFullReductionEntities.forEach(skuFullReductionEntity -> {
            saleVOS.add(new SaleVO("满减", "满" + skuFullReductionEntity.getFullPrice() + "减" + skuFullReductionEntity.getReducePrice()));
        });

        return saleVOS;
    }

}