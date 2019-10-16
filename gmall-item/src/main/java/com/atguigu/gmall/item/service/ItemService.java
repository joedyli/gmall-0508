package com.atguigu.gmall.item.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.sms.vo.SaleVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVO queryItemVO(Long skuId) {

        ItemVO itemVO = new ItemVO();
        itemVO.setSkuId(skuId);

        // 根据skuId查询sku的信息
        CompletableFuture<SkuInfoEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.skuInfo(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            itemVO.setTitle(skuInfoEntity.getSkuTitle());
            itemVO.setSubTitle(skuInfoEntity.getSkuSubtitle());
            itemVO.setPrice(skuInfoEntity.getPrice());
            itemVO.setWeight(skuInfoEntity.getWeight());
            return skuInfoEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 根据skuId查询sku图片
            Resp<List<SkuImagesEntity>> listResp = this.gmallPmsClient.querySkuImages(skuId);
            List<SkuImagesEntity> skuImagesEntities = listResp.getData();
            itemVO.setImages(skuImagesEntities);
        }, threadPoolExecutor);


        CompletableFuture<Void> catFutrue = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据sku中的cateLogId查询分类
            Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.categoryInfo(skuInfoEntity.getCatalogId());
            CategoryEntity categoryEntity = categoryEntityResp.getData();
            itemVO.setCid3(categoryEntity.getCatId());
            itemVO.setCategoryName(categoryEntity.getName());
        }, threadPoolExecutor);


        CompletableFuture<Void> brandFutrue = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据sku中的brandId查询品牌
            Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.brandInfo(skuInfoEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResp.getData();
            itemVO.setBrandId(brandEntity.getBrandId());
            itemVO.setBrandName(brandEntity.getName());
        }, threadPoolExecutor);


        CompletableFuture<Void> spuFutrue = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据sku中的spuId查询spu
            Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.spuInfo(skuInfoEntity.getSpuId());
            SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
            itemVO.setSpuId(spuInfoEntity.getId());
            itemVO.setSpuName(spuInfoEntity.getSpuName());
        }, threadPoolExecutor);

        CompletableFuture<Void> saleFuture = CompletableFuture.runAsync(() -> {
            // 根据skuid查询营销信息
            Resp<List<SaleVO>> saleVOResp = this.gmallSmsClient.querySaleVObySkuId(skuId);
            List<SaleVO> saleVOS = saleVOResp.getData();
            itemVO.setSales(saleVOS);
        }, threadPoolExecutor);


        CompletableFuture<Void> skuSaleFutrue = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据sku中spuId查询所有sku的销售属性
            Resp<List<SkuSaleAttrValueEntity>> skuSaleAttrValueResp = this.gmallPmsClient.querySaleAttrVOSBySpuId(skuInfoEntity.getSpuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleAttrValueResp.getData();
            itemVO.setSaleAttrs(skuSaleAttrValueEntities);
        }, threadPoolExecutor);

        CompletableFuture<Void> attrFutrue = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据spuId查询通用的规格参数及值
            Resp<List<ProductAttrValueEntity>> baseAttrValueResp = this.gmallPmsClient.queryBySpuId(skuInfoEntity.getSpuId());
            List<ProductAttrValueEntity> productAttrValueEntities = baseAttrValueResp.getData();
            itemVO.setBaseAttrs(productAttrValueEntities);
        }, threadPoolExecutor);

        CompletableFuture<Void> spuDescFutrue = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据spuId查询spuInfoDesc
            Resp<SpuInfoDescEntity> infoDescEntityResp = this.gmallPmsClient.spuinfodesc(skuInfoEntity.getSpuId());
            SpuInfoDescEntity spuInfoDescEntity = infoDescEntityResp.getData();
            itemVO.setDescription(Arrays.asList(StringUtils.split(spuInfoDescEntity.getDecript())));
        }, threadPoolExecutor);

        CompletableFuture<Void> groupFutrue = skuFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据sku的分类id查询组及组下属性和值
            Resp<List<GroupVO>> groupVOResp = this.gmallPmsClient.queryGroupWithAttrValueByCid(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId(), skuId);
            List<GroupVO> groupVOS = groupVOResp.getData();
            itemVO.setGroups(groupVOS);
        }, threadPoolExecutor);

        CompletableFuture<Void> future = CompletableFuture.allOf(skuFuture, imageFuture, catFutrue, brandFutrue,
                spuFutrue, saleFuture, skuSaleFutrue, attrFutrue, spuDescFutrue, groupFutrue);
        future.join();

        return itemVO;
    }
}
