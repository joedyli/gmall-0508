package com.atguigu.gmall.auth.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("auth")
@EnableConfigurationProperties({JwtProperties.class})
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("accredit")
    public Resp<Object> accredit(@RequestParam("username")String username, @RequestParam("password")String password, HttpServletResponse response, HttpServletRequest request) throws Exception {

        // 查询用户信息，并制作jwt字符串token
        String jwtToken = this.authService.queryUser(username, password);

        if (StringUtils.isBlank(jwtToken)){
            return Resp.fail("用户名或者密码错误！");
        }

        // 放到cookie中
//        Cookie cookie = new Cookie(this.jwtProperties.getCookieName(), jwtToken);
//        cookie.setDomain("");
//        cookie.setMaxAge(this.jwtProperties.getExpire() * 60);
//        response.addCookie(cookie);
        CookieUtils.setCookie(request, response, this.jwtProperties.getCookieName(), jwtToken, this.jwtProperties.getExpire() * 60);

        return Resp.ok(null);
    }

}
