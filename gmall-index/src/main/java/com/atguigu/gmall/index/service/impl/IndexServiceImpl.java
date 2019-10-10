package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.CategoryVO;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "index:cats:";

    private static final Integer TIMEOUT = 30;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public List<CategoryEntity> queryLvl1Cates() {

        Resp<List<CategoryEntity>> listResp = this.gmallPmsClient.queryCategoriesByCidOrLevel(1, null);

        return listResp.getData();
    }

    @Override
    public List<CategoryVO> queryCatesByPid(Long pid) {

        // 1. 先查询缓存，缓存中有直接返回
        String catJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(catJson)) {
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
    public void testLock() {

        RLock lock = this.redissonClient.getLock("lock");
        lock.lock(30, TimeUnit.SECONDS);

        String num = this.redisTemplate.opsForValue().get("num");

        if (StringUtils.isEmpty(num)) {
            this.redisTemplate.opsForValue().set("num", "1");
            num = "1";
        }

        Integer n = Integer.parseInt(num);
        n++;
        this.redisTemplate.opsForValue().set("num", n.toString());

        //lock.unlock();
    }

    public void testLock1() {

        String value = UUID.randomUUID().toString(); // 加锁时，为每个线程设置一个唯一值
        // 获取分布式锁
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", value, 15, TimeUnit.MILLISECONDS);

        if (lock) {

            String num = this.redisTemplate.opsForValue().get("num");

            if (StringUtils.isEmpty(num)) {
                this.redisTemplate.opsForValue().set("num", "1");
                num = "1";
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Integer n = Integer.parseInt(num);
            n++;
            this.redisTemplate.opsForValue().set("num", n.toString());

            // 释放分布式锁，判断当前的lock的值还是不是，我自己的那个锁的唯一值
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"), value);
            /*if(StringUtils.equals(this.redisTemplate.opsForValue().get("lock"), value)) {
                // 判断完成之后，删除之前，自己的锁过期了，也会删到别人的锁
                this.redisTemplate.delete("lock");
            }*/
        } else {

            // 重试
            try {
                Thread.sleep(1000);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
