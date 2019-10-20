package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2019-09-21 11:34:08
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    List<WareSkuEntity> queryStock(@Param("skuId")Long skuId, @Param("num") Integer num);

    int lockStock(@Param("wareSkuId")Long wareSkuId, @Param("num") Integer num);

    int unLockStock(@Param("wareSkuId")Long wareSkuId, @Param("num") Integer num);

    void minusStock(@Param("wareSkuId")Long wareSkuId, @Param("num") Integer num);
}
