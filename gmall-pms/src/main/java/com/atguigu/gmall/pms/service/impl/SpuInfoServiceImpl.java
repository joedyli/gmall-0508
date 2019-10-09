package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuInfoDescEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuByKeyPage(QueryCondition condition, Long catId) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        // 判断catId是否为null
        if (catId != 0) {
            wrapper.eq("catalog_id", catId);
        }

        // 判断用户是否输入了查询条件
        String key = condition.getKey();
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> {
                // 默认情况下，所有条件都是and关系，并且是没有括号
                // or().拼接条件，变成or关系
                // or(t->{t.拼接条件})
                // and(t->{t.拼接条件})
                return t.eq("id", key).or().like("spu_name", key);
            });
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);
    }

//    @Transactional(rollbackFor = FileNotFoundException.class, noRollbackFor = ArithmeticException.class, timeout = 3, readOnly = true)
    @GlobalTransactional
    @Override
    public void saveSpuWithSku(SpuInfoVO spuInfoVO) throws FileNotFoundException {

        // 1. 保存spu相关的信息
        // 1.1.  保存spuInfo
        Long spuId = saveSpuInfo(spuInfoVO);

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // 1.2.  保存spuInfoDesc == spuImages
        saveSpuInfoDesc(spuInfoVO, spuId);

        // 1.3.  保存product_attr_value == baseAttrs
        saveBaseAttr(spuInfoVO, spuId);

        // 2. 保存sku相关的
        skuInfoService.saveSkuInfo(spuInfoVO, spuId);

        //FileInputStream xxxx = new FileInputStream(new File("xxxx"));
//        int i = 1/0;
    }

    @Transactional
    public void saveBaseAttr(SpuInfoVO spuInfoVO, Long spuId) {
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(baseAttr -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                productAttrValueEntity.setAttrName(baseAttr.getAttrName());
                productAttrValueEntity.setSpuId(spuId);
                productAttrValueEntity.setAttrValue(baseAttr.getAttrValue());
                productAttrValueEntity.setAttrSort(1);
                productAttrValueEntity.setQuickShow(0);
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(productAttrValueEntities);
        }
    }

    @Transactional
    public void saveSpuInfoDesc(SpuInfoVO spuInfoVO, Long spuId) {
        if (!CollectionUtils.isEmpty(spuInfoVO.getSpuImages())) {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setSpuId(spuId);
            spuInfoDescEntity.setDecript(StringUtils.join(spuInfoVO.getSpuImages(), ","));
            this.spuInfoDescDao.insert(spuInfoDescEntity);
        }
    }

    @Transactional
    public Long saveSpuInfo(SpuInfoVO spuInfoVO) {
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoVO, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUodateTime(spuInfoEntity.getCreateTime());
        this.save(spuInfoEntity);
        return spuInfoEntity.getId();
    }

}