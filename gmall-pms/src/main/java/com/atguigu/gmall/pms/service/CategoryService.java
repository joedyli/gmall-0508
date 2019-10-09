package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.CategoryVO;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品三级分类
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2019-09-21 11:05:50
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageVo queryPage(QueryCondition params);

    /**
     * 根据分类等级或者分类的父id查询分类信息
     * @param level
     * @param parentCid
     * @return
     */
    List<CategoryEntity> queryCategoriesByCidOrLevel(Integer level, Long parentCid);

    List<CategoryVO> querySubCategories(Long pid);
}

