package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.sms.vo.SaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemVO {

    // 分类 sku.getCatId --> 根据catId查询分类
    private Long cid3;
    private String categoryName;

    // 品牌 sku.getBrandId --> 根据brandId查询品牌
    private Long brandId;
    private String brandName;

    // spu信息 sku.getSpuId --> 根据spuId查询Spu
    private Long spuId;
    private String spuName;

    // sku信息，skuId-->sku
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private BigDecimal weight;

    // 促销 O  skuId-->根据skuId直接查询三张表
    private List<SaleVO> sales;

    // 销售属性,是sku所在的spu下的所有销售属性 sku.getSpuId-->根据spuId查询所有sku的销售属性
    private List<SkuSaleAttrValueEntity> saleAttrs;

    // sku的图片信息，根据skuId-->当前sku的图片
    private List<SkuImagesEntity> images;

    // 通用的规格参数及值，sku.getSpuId-->根据spuId查询通用的规格参数及值
    private List<ProductAttrValueEntity> baseAttrs;

    // spu的描述信息，spuId-->根据spuId查询spuInfoDesc O  X
    private List<String> description;

    // 规格参数组，及组下的规格参数 catId-->group-->relations-->attr-->attrValue(baseAttrValue, skuAttrValue)   O X
    private List<GroupVO> groups;


}
