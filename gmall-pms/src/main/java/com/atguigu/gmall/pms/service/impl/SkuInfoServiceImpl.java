package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.feign.GmallSmsFeign;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.gmall.sms.vo.SkuSaleVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.service.SkuInfoService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private GmallSmsFeign smsFeign;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    public void saveSkuInfo(SpuInfoVO spuInfoVO, Long spuId) {
        List<SkuInfoVO> skuInfoVOS = spuInfoVO.getSkus();
        if (CollectionUtils.isEmpty(skuInfoVOS)){
            return;
        }

        skuInfoVOS.forEach(skuInfoVO -> {
            // 2.1.  保存skuInfo
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(skuInfoVO, skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
            List<String> images = skuInfoVO.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                skuInfoEntity.setSkuDefaultImg(StringUtils.isEmpty(skuInfoEntity.getSkuDefaultImg()) ? images.get(0) : skuInfoEntity.getSkuDefaultImg());
            }
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString().toUpperCase());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            this.save(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();

            // 2.2.  保存skuImages == sku.images
            if (!CollectionUtils.isEmpty(images)) {
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setImgSort(1);
                    skuImagesEntity.setDefaultImg(StringUtils.equals(skuInfoEntity.getSkuDefaultImg(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImagesEntities);
            }

            // 2.3.  保存sku_sale_attr_value  == sku.saleAttrs
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(saleAttr -> {
                    saleAttr.setSkuId(skuId);
                    saleAttr.setAttrSort(1);
                });
                this.skuSaleAttrValueService.saveBatch(saleAttrs);
            }


            // 3. 保存优惠相关
            SkuSaleVO skuSaleVO = new SkuSaleVO();
            BeanUtils.copyProperties(skuInfoVO, skuSaleVO);
            skuSaleVO.setSkuId(skuId);
            this.smsFeign.saveSale(skuSaleVO);
        });
    }

}