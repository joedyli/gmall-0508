package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {

        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<>();
        // 1，用户名；2，手机；3，邮箱
        if (type == 1) {
            wrapper.eq("username", data);
        } else if (type == 2) {
            wrapper.eq("mobile", data);
        } else if (type == 3) {
            wrapper.eq("email", data);
        } else {
            return false;
        }
        return this.count(wrapper) == 0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) throws IllegalAccessException {
        // 1.校验验证码：TODO
        if(!StringUtils.equals(code, "123456")){
            throw new IllegalAccessException("验证码错误");
        }

        // 2.生成盐
        String salt = UUID.randomUUID().toString().substring(0, 5);


        // 3.对密码加密存储
        memberEntity.setPassword(DigestUtils.md5Hex(memberEntity.getPassword() + salt));

        memberEntity.setSalt(salt);

        // 4.保存用户信息到数据库
        memberEntity.setSourceType(1);
        memberEntity.setIntegration(0);
        memberEntity.setGrowth(0);
        memberEntity.setCreateTime(new Date());
        memberEntity.setStatus(1);
        this.save(memberEntity);

        // 5.删除redis中的验证码：TODO
    }

    @Override
    public MemberEntity query(String username, String password) throws IllegalAccessException {

        // 先根据用户名查询用户信息
        MemberEntity user = this.getOne(new QueryWrapper<MemberEntity>().eq("username", username));

        // 判断用户是否为空
        if (user == null) {
            throw new IllegalAccessException("用户名不存在！");
        }

        // 获取用户信息中的盐，对登陆时的密码进行相同方式的加密
        password = DigestUtils.md5Hex(password + user.getSalt());

        // 比较登录密码和数据库密码是否一致
        if (!StringUtils.equals(password, user.getPassword())){
            throw new IllegalAccessException("密码不正确！");
        }

        return user;
    }

}