package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

public interface IndexService {

    List<CategoryEntity> queryLvl1Cates();

    List<CategoryVO> queryCatesByPid(Long pid);

    void testLock();

    String testread();

    String testwrite();

    String testlatch() throws InterruptedException;

    String testout();
}
