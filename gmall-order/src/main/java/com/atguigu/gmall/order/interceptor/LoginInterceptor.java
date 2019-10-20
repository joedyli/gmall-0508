package com.atguigu.gmall.order.interceptor;

import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.order.config.JwtProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties({JwtProperties.class})
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties jwtProperties;

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfo userInfo = new UserInfo();

        //获取cookie中的token信息
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        // 判断如果用户token为空，并且游客userKey为空，生成一个UserKey，保存到cookie中
        if (StringUtils.isBlank(token)) {
            // 跳转到登录页
            response.sendRedirect("http://localhost:2000/login.html?returnUrl=" + request.getRequestURL());
        } else {
            try {
                // 解析
                Map<String, Object> map = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
                // 如果登陆了，放userId到threadlocal中
                userInfo.setUserId(Long.valueOf(map.get("id").toString()));
                THREAD_LOCAL.set(userInfo);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("http://localhost:2000/login.html?returnUrl=" + request.getRequestURL());
            }
        }

        return true;
    }

    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 释放threadlocal中的线程变量，因为使用的是tomcat线程池
        THREAD_LOCAL.remove();
    }
}
