package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.io.FileNotFoundException;


/**
 * spu信息
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2019-09-21 11:05:50
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo querySpuByKeyPage(QueryCondition condition, Long catId);

    void saveSpuWithSku(SpuInfoVO spuInfo) throws FileNotFoundException;

    void saveBaseAttr(SpuInfoVO spuInfoVO, Long spuId);

    void saveSpuInfoDesc(SpuInfoVO spuInfoVO, Long spuId);

    Long saveSpuInfo(SpuInfoVO spuInfoVO);

}

