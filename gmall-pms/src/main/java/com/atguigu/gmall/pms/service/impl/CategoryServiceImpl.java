package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.CategoryVO;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.CategoryDao;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryDao categoryDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoriesByCidOrLevel(Integer level, Long parentCid) {

        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        // 判断level是否为0
        if (level != 0) {
            queryWrapper.eq("cat_level", level);
        }

        // 判断父节点id是否为null
        if (parentCid != null) {
            queryWrapper.eq("parent_cid", parentCid);
        }

        // 执行查询
        List<CategoryEntity> categoryEntities = this.categoryDao.selectList(queryWrapper);

        return categoryEntities;
    }

    @Override
    public List<CategoryVO> querySubCategories(Long pid){

        return this.categoryDao.querySubCategories(pid);
    }

}