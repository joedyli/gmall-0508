package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemVO {

    // 分类 O X
    private Long cid3;
    private String categoryName;

    // 品牌 O X
    private Long brandId;
    private String brandName;

    // spu信息 O X
    private Long spuId;
    private String spuName;

    // sku信息 O  X
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private BigDecimal weight;

    // 促销 O  ?
    private List<SaleVO> sales;

    // 销售属性  O  ?
    private List<SkuSaleAttrValueEntity> saleAttrs;

    // sku的图片信息 O  ?
    private List<SkuImagesEntity> images;

    // 通用的规格参数及值 O  X
    private List<ProductAttrValueEntity> baseAttrs;

    // spu的描述信息 O  X
    private List<String> description;

    // 规格参数组，及组下的规格参数   O X
    private List<GroupVO> groups;


}
