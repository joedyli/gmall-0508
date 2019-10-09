package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.CategoryVO;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "index:cats:";

    private static final Integer TIMEOUT = 30;

    @Override
    public List<CategoryEntity> queryLvl1Cates() {

        Resp<List<CategoryEntity>> listResp = this.gmallPmsClient.queryCategoriesByCidOrLevel(1, null);

        return listResp.getData();
    }

    @Override
    public List<CategoryVO> queryCatesByPid(Long pid) {

        // 1. 先查询缓存，缓存中有直接返回
        String catJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(catJson)){
            List<CategoryVO> categoryVOS = JSON.parseArray(catJson, CategoryVO.class);
            return categoryVOS;
        }

        // 2. 查询数据库
        Resp<List<CategoryVO>> listResp = this.gmallPmsClient.queryCategorysWithSub(pid);
        List<CategoryVO> categoryVOS = listResp.getData();

        // 3. 放入缓存
        this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryVOS), TIMEOUT + new Random(10).nextInt(), TimeUnit.DAYS);

        return categoryVOS;
    }

    @Override
    public synchronized void testLock() {

        String num = this.redisTemplate.opsForValue().get("num");
        if (StringUtils.isEmpty(num)){
            this.redisTemplate.opsForValue().set("num", "1");
        }
        Integer n = Integer.parseInt(num);
        n++;
        this.redisTemplate.opsForValue().set("num", n.toString());
    }
}
