package com.atguigu.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SpuAttributeValueVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SpuInfoListener {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private JestClient jestClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "GMALL-SPUINFO-QUEUE", durable = "true"),
            exchange = @Exchange(value = "GMALL-ITEM-EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.insert", "item.update"}
    ))
    public void listener(Map<String, Object> map) {

        Long spuId = (Long) map.get("spuId");
        if (spuId != null) {
            Resp<List<SkuInfoEntity>> skuResp = this.gmallPmsClient.querySkuBySpuId(spuId);
            List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
            if (!CollectionUtils.isEmpty(skuInfoEntities)) {

                skuInfoEntities.forEach(skuInfoEntity -> {
                    GoodsVO goodsVO = new GoodsVO();
                    // sku的基本信息
                    goodsVO.setId(skuInfoEntity.getSkuId());
                    goodsVO.setName(skuInfoEntity.getSkuTitle());
                    goodsVO.setSort(0);
                    goodsVO.setSale(0);
                    goodsVO.setPrice(skuInfoEntity.getPrice());
                    goodsVO.setPic(skuInfoEntity.getSkuDefaultImg());

                    // 品牌相关信息
                    Resp<BrandEntity> brandResp = this.gmallPmsClient.brandInfo(skuInfoEntity.getBrandId());
                    BrandEntity brandEntity = brandResp.getData();
                    goodsVO.setBrandId(brandEntity.getBrandId());
                    goodsVO.setBrandName(brandEntity.getName());

                    // 分类相关信息
                    Resp<CategoryEntity> categoryResp = this.gmallPmsClient.categoryInfo(skuInfoEntity.getCatalogId());
                    CategoryEntity categoryEntity = categoryResp.getData();
                    goodsVO.setProductCategoryId(categoryEntity.getCatId());
                    goodsVO.setProductCategoryName(categoryEntity.getName());

                    // 库存信息
                    Resp<List<WareSkuEntity>> wareResp = this.gmallWmsClient.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
                    List<WareSkuEntity> wareSkuEntities = wareResp.getData();
                    goodsVO.setStock(0l);
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        wareSkuEntities.forEach(wareSkuEntity -> {
                            // 只要有一个仓库有该sku的库存，就可以搜索出来
                            if (wareSkuEntity.getStock() > 0) {
                                goodsVO.setStock(100l);
                            }
                        });
                    }

                    // 设置搜索属性
                    Resp<List<ProductAttrValueEntity>> attrResp = this.gmallPmsClient.queryBySpuId(spuId);
                    List<ProductAttrValueEntity> productAttrValueEntities = attrResp.getData();
                    if (!CollectionUtils.isEmpty(productAttrValueEntities)) {
                        List<SpuAttributeValueVO> attrValueList = productAttrValueEntities.stream().map(productAttrValueEntity -> {
                            SpuAttributeValueVO spuAttributeValueVO = new SpuAttributeValueVO();
                            spuAttributeValueVO.setSpuId(productAttrValueEntity.getSpuId());
                            spuAttributeValueVO.setValue(productAttrValueEntity.getAttrValue());
                            spuAttributeValueVO.setName(productAttrValueEntity.getAttrName());
                            spuAttributeValueVO.setId(productAttrValueEntity.getId());
                            spuAttributeValueVO.setAttrId(productAttrValueEntity.getAttrId());
                            return spuAttributeValueVO;
                        }).collect(Collectors.toList());
                        goodsVO.setAttrValueList(attrValueList);
                    }

                    Index action = new Index.Builder(goodsVO).index("goods").type("info").id(skuInfoEntity.getSkuId().toString()).build();
                    try {
                        this.jestClient.execute(action);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
