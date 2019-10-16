package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.atguigu.gmall.pms.vo.AttrValueVO;
import com.atguigu.gmall.pms.vo.GroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryAttrGroupByCid(Long cateId, QueryCondition queryCondition) {

        // 构建查询条件
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();

        // 拼接查询条件
        if (cateId != null) {
            queryWrapper.eq("catelog_id", cateId);
        }

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(queryCondition),
                queryWrapper
        );

        return new PageVo(page);
    }

    @Override
    public AttrGroupVO queryGroupWithAttrByGid(Long gid) {

        AttrGroupVO attrGroupVO = new AttrGroupVO();

        // 先查询attrGroup
        AttrGroupEntity attrGroupEntity = this.attrGroupDao.selectById(gid);
        BeanUtils.copyProperties(attrGroupEntity, attrGroupVO);

        // 再去查询AttrAttrGroup中间表
        List<AttrAttrgroupRelationEntity> relationEntities = this.relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        if (CollectionUtils.isEmpty(relationEntities)){
            return attrGroupVO;
        }
        attrGroupVO.setRelations(relationEntities);

        // 最后查询Attr规格参数 map
        List<Long> idList = relationEntities.stream().map(relationEntity -> relationEntity.getAttrId()).collect(Collectors.toList());
        List<AttrEntity> attrEntities = this.attrDao.selectBatchIds(idList);
        attrGroupVO.setAttrEntities(attrEntities);

        return attrGroupVO;
    }

    @Override
    public List<AttrGroupVO> queryGroupWithAttrByCid(Long catId) {

        // 根据分类id查询所有组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));

        // 判空
        if(CollectionUtils.isEmpty(attrGroupEntities)){
            return null;
        }

        return attrGroupEntities.stream().map(attrGroupEntity -> {
            return this.queryGroupWithAttrByGid(attrGroupEntity.getAttrGroupId());
        }).collect(Collectors.toList());
    }

    @Override
    public List<GroupVO> queryGroupWithAttrValueByCid(Long catId, Long spuId, Long skuId) {

        // 根据分类的id查询组及组下的规格参数
        List<AttrGroupVO> attrGroupVOS = this.queryGroupWithAttrByCid(catId);

        return attrGroupVOS.stream().map(attrGroupVO -> {
            GroupVO groupVO = new GroupVO();

            List<AttrEntity> attrEntities = attrGroupVO.getAttrEntities();
            if (!CollectionUtils.isEmpty(attrEntities)){
                List<Long> attrIds = attrEntities.stream().map(attrEntity -> attrEntity.getAttrId()).collect(Collectors.toList());

                // 根据attrId和spuId查询规格属性值
                List<ProductAttrValueEntity> productAttrValueEntities = this.productAttrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>().in("attr_id", attrIds).eq("spu_id", spuId));
                // 根据attrId和skuId查询销售属性值
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = this.skuSaleAttrValueDao.selectList(new QueryWrapper<SkuSaleAttrValueEntity>().in("attr_id", attrIds).eq("sku_id", skuId));

                List<AttrValueVO> attrValueVOS = new ArrayList<>();

                // 通用的属性值
                if (!CollectionUtils.isEmpty(productAttrValueEntities)){
                    for (ProductAttrValueEntity productAttrValueEntity : productAttrValueEntities) {
                        attrValueVOS.add(new AttrValueVO(productAttrValueEntity.getAttrId(), productAttrValueEntity.getAttrName(), Arrays.asList(productAttrValueEntity.getAttrValue().split(","))));
                    }

                }
                // 特殊的属性值
                if (!CollectionUtils.isEmpty(skuSaleAttrValueEntities)){
                    for (SkuSaleAttrValueEntity skuSaleAttrValueEntity : skuSaleAttrValueEntities) {
                        attrValueVOS.add(new AttrValueVO(skuSaleAttrValueEntity.getAttrId(), skuSaleAttrValueEntity.getAttrName(), Arrays.asList(skuSaleAttrValueEntity.getAttrValue())));
                    }
                }
                groupVO.setAttrs(attrValueVOS);
            }
            groupVO.setGroupName(attrGroupVO.getAttrGroupName());
            return groupVO;
        }).collect(Collectors.toList());

    }

}