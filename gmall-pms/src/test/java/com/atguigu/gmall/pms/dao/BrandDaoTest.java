package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BrandDaoTest {

    @Autowired
    private BrandDao brandDao;

    @Test
    public void test(){
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("尚硅谷");
//        brandEntity.setDescript("尚硅谷就是好");
//        brandEntity.setFirstLetter("S");
//        this.brandDao.insert(brandEntity);
        this.brandDao.selectList(new QueryWrapper<BrandEntity>().like("name", "%硅谷%")).forEach(System.out::println);
    }

}
