package com.atguigu.gmall.gateway.config;

import com.atguigu.core.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@EnableConfigurationProperties({JwtProperties.class})
public class AuthGatewayFilter implements GatewayFilter, Ordered {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 获取jwt类型的token
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies == null || cookies.isEmpty() || !cookies.containsKey(this.jwtProperties.getCookieName())) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 拦截后续的业务逻辑
            return response.setComplete();
        }
        HttpCookie cookie = cookies.getFirst(this.jwtProperties.getCookieName());

        try {
            // 解析jwt，正常。成功
            JwtUtils.getInfoFromToken(cookie.getValue(), this.jwtProperties.getPublicKey());
            return chain.filter(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 拦截后续的业务逻辑
            return response.setComplete();
        }

    }

    /**
     * 返回值越小，优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return 1;
    }
}
