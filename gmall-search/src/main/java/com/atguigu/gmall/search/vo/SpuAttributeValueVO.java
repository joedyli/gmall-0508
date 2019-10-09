package com.atguigu.gmall.search.vo;

import lombok.Data;

@Data
public class SpuAttributeValueVO {

    private Long id;  //商品和属性关联的数据表的主键id
    private Long attrId; //当前sku对应的属性的attr_id
    private String name;//属性名  电池
    private String value;//3G   3000mah
    private Long spuId;//这个属性关系对应的spu的id
}
