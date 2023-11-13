package com.heima.admin.gateway.filters;

import com.heima.admin.gateway.utils.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthorizeFilter implements Ordered, GlobalFilter {

    /**
     * 处理对jwt令牌对校验
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取request、response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 对 /login 路径放行
        if(request.getURI().getPath().contains("/login")) {
            return chain.filter(exchange);
        }
        // 获取token令牌
        String token = request.getHeaders().getFirst("token");

        // 为null，拦截
        if(StringUtils.isEmpty(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        try {
            // 校验token
            Claims claims = AppJwtUtil.getClaimsBody(token);
            int result = AppJwtUtil.verifyToken(claims);
            // 无效 -> 拦截，并且返回状态码404
            if(result == 1 || result == 2) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            //获得token解析后中的用户信息
            Object userId = claims.get("id");
            //在header中添加新的信息
            ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                httpHeaders.add("userId", userId + "");
            }).build();
            //重置header
            exchange.mutate().request(serverHttpRequest).build();
        } catch (Exception e) {
            log.info(e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        }
        // 放行
        return chain.filter(exchange);

    }


    /**
     * 配置优先级，越小越高
     * @return
     */
    public int getOrder() {
        return 0;
    }
}
