package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.entity.SkuLockVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "sku:lock:";
    private static final String STOCK_PREFIX = "sku:stock:";

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public String checkAndLockStock(List<SkuLockVO> skuLockVOS) {

        List<SkuLockVO> notLockStock = new ArrayList<>(); // 未能锁定库存
        List<SkuLockVO> LockStock = new ArrayList<>(); // 已锁定的库存

        skuLockVOS.forEach(skuLockVO -> {
            RLock lock = this.redissonClient.getFairLock(LOCK_PREFIX + skuLockVO.getSkuId());
            lock.lock();
            // 查询
            List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.queryStock(skuLockVO.getSkuId(), skuLockVO.getNum());
            if (CollectionUtils.isEmpty(wareSkuEntities)){
                notLockStock.add(skuLockVO);
            } else {
                // 锁定，就是更新锁定库存的数量
                Long wareSkuId = wareSkuEntities.get(0).getId();
                int count = this.wareSkuDao.lockStock(wareSkuId, skuLockVO.getNum());
                if (count == 1) {
                    skuLockVO.setWareSkuId(wareSkuId);
                    LockStock.add(skuLockVO);
                } else {
                    notLockStock.add(skuLockVO);
                }
            }
            lock.unlock();
        });

        // 锁定失败
        if (notLockStock.size() != 0) {
            // 解除锁定
            LockStock.forEach(lockStockVO -> {
                this.wareSkuDao.unLockStock(lockStockVO.getWareSkuId(), lockStockVO.getNum());
            });

            // 记录锁定失败的商品
            List<Long> skuIds = notLockStock.stream().map(lockStock -> lockStock.getSkuId()).collect(Collectors.toList());
            return "锁定失败，锁定失败的商品id: " + skuIds.toString();
        }

        // 把已锁定库存信息保存到redis中，方便以后解锁
        this.redisTemplate.opsForValue().set(STOCK_PREFIX + skuLockVOS.get(0).getOrderToken(), JSON.toJSONString(LockStock));

        return null;
    }

    @Override
    public void unlockStock(String orderToken) {
        String lockStockJson = this.redisTemplate.opsForValue().get(STOCK_PREFIX + orderToken);
        List<SkuLockVO> skuLockVOS = JSON.parseArray(lockStockJson, SkuLockVO.class);
        // 解除锁定
        skuLockVOS.forEach(lockStockVO -> {
            this.wareSkuDao.unLockStock(lockStockVO.getWareSkuId(), lockStockVO.getNum());
        });
    }

    @Override
    public void minus(String orderToken) {
        String lockStockJson = this.redisTemplate.opsForValue().get(STOCK_PREFIX + orderToken);
        List<SkuLockVO> skuLockVOS = JSON.parseArray(lockStockJson, SkuLockVO.class);
        // 遍历减库存
        skuLockVOS.forEach(lockStockVO -> {
            this.wareSkuDao.minusStock(lockStockVO.getWareSkuId(), lockStockVO.getNum());
        });
    }

}