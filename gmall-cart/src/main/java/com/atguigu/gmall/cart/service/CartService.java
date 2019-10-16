package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.interceptor.UserInfo;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final String KEY_PREFIX = "gmall:cart:";

    private static final String PRICE_PREFIX = "gmall:price:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    public void addCart(Cart cart) {

        // 判断用户的登录状态
        String key = getKey();

        // 加入购物车
        // 取出该用户的购物车
        BoundHashOperations<String, Object, Object> ops = this.redisTemplate.boundHashOps(key);

        // 取出用户传递过来的数量
        Integer num = cart.getCount();
        Long skuId = cart.getSkuId();

        // 判断购物车中是否有该条购物车记录
        if (ops.hasKey(skuId.toString())){
            // 有，更新数量
            String cartJson = ops.get(skuId.toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount() + num);
        } else {
            // 无，新增购物车记录
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.skuInfo(skuId);
            Resp<List<SkuSaleAttrValueEntity>> resp = this.gmallPmsClient.querySaleAttrValuesBySkuId(skuId);
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = resp.getData();
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setSkuAttrValue(skuSaleAttrValueEntities);
            // TODO:自己完成
            cart.setSkuSaleVO(null);
            cart.setTitle(skuInfoEntity.getSkuTitle());

            this.redisTemplate.opsForValue().setIfAbsent(PRICE_PREFIX + skuId, skuInfoEntity.getPrice().toString());
        }
        // 放回redis数据库中
        ops.put(skuId.toString(), JSON.toJSONString(cart));
    }

    public List<Cart> queryCarts() {

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key1 = KEY_PREFIX + userInfo.getUserKey();
        // 查询未登录的购物车
        BoundHashOperations<String, Object, Object> userKeyOps = this.redisTemplate.boundHashOps(key1);
        List<Object> cartJsonList = userKeyOps.values();
        List<Cart> carts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cartJsonList)){
            carts = cartJsonList.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String price = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(price));
                return cart;
            }).collect(Collectors.toList());
        }

        // 判断是否登录
        if (userInfo.getUserId() == null) {
            // 未登录，返回
            return carts;
        }

        // 登录，获取登录状态购物车的操作对象
        String key2 = KEY_PREFIX + userInfo.getUserId();
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key2);

        // 便利未登陆的购物车，往登录状态的购物车中添加
        carts.forEach(cart -> {
            String skuId = cart.getSkuId().toString();
            Integer num = cart.getCount();
            if(userIdOps.hasKey(skuId)){
                // 如果已登录购物车包含了该条未登录购物车记录，就更新已登录购物车的数量
                String cartJson = userIdOps.get(skuId).toString();
                cart = JSON.parseObject(cartJson, Cart.class);
                cart.setCount(cart.getCount() + num);
            }
            userIdOps.put(skuId, JSON.toJSONString(cart));
        });

        // 删除未登录的购物车
        this.redisTemplate.delete(key1);

        // 查询合并过的购物车
        List<Object> userCartJsonList = userIdOps.values();
        List<Cart> userCarts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userCartJsonList)){
            userCarts = userCartJsonList.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String price = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(price));
                return cart;
            }).collect(Collectors.toList());
        }

        return userCarts;
    }

    public void updateCart(Cart cart) {

        String key = getKey();
        BoundHashOperations<String, Object, Object> ops = this.redisTemplate.boundHashOps(key);

        // 获取用户输入的购物车数量
        Integer num = cart.getCount();

        // 获取数据库中要更新的购物车记录
        String cartJson = ops.get(cart.getSkuId().toString()).toString();
        // 反序列化
        cart = JSON.parseObject(cartJson, Cart.class);
        cart.setCount(num); // 更新数量
        ops.put(cart.getSkuId().toString(), JSON.toJSONString(cart));// 保存
    }

    public void deleteCart(Long skuId) {

        String key = getKey();

        BoundHashOperations<String, Object, Object> ops = this.redisTemplate.boundHashOps(key);

        if(ops.hasKey(skuId.toString())){
            ops.delete(skuId.toString());
        }
    }

    private String getKey() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX;
        if (userInfo.getUserId() != null){
            key = key + userInfo.getUserId();
        } else {
            key = key + userInfo.getUserKey();
        }
        return key;
    }


}
