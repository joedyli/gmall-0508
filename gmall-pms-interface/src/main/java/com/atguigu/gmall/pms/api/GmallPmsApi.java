package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.entity.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    @GetMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> querySkuBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> brandInfo(@PathVariable("brandId") Long brandId);

    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> categoryInfo(@PathVariable("catId") Long catId);

    @PostMapping("pms/spuinfo/page")
    public Resp<List<SpuInfoEntity>> querySpuByPageAndSale(@RequestBody QueryCondition condition);

    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<ProductAttrValueEntity>> queryBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/category")
    public Resp<List<CategoryEntity>> queryCategoriesByCidOrLevel(@RequestParam(value="level", defaultValue = "0")Integer level
            , @RequestParam(value="parentCid", required = false)Long parentCid);

    @GetMapping("pms/category/{pid}")
    public Resp<List<CategoryVO>> queryCategorysWithSub(@PathVariable("pid")Long pid);

    @GetMapping("pms/skuinfo/info/{skuId}")
    public Resp<SkuInfoEntity> skuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/spuinfo/info/{id}")
    public Resp<SpuInfoEntity> spuInfo(@PathVariable("id") Long id);

    @GetMapping("pms/spuinfodesc/info/{spuId}")
    public Resp<SpuInfoDescEntity> spuinfodesc(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/attrgroup/withattrs/cat/{catId}")
    public Resp<List<AttrGroupVO>> queryGroupWithAttrByCid(@PathVariable("catId")Long catId);
}
