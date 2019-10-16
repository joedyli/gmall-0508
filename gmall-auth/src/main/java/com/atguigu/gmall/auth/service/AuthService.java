package com.atguigu.gmall.auth.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.ums.entity.MemberEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private GmallUmsClient gmallUmsClient;

    @Autowired
    private JwtProperties jwtProperties;

    public String queryUser(String username, String password) throws Exception {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return null;
        }
        Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.query(username, password);
        MemberEntity memberEntity = memberEntityResp.getData();

        // 判断用户是否为空
        if (memberEntity == null){
            return null;
        }

        // 构建jwt的token信息
        Map<String, Object> map = new HashMap<>();
        map.put("id", memberEntity.getId());
        map.put("username", memberEntity.getUsername());
        return JwtUtils.generateToken(map, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());
    }
}
