package com.ruoyi.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.ServletUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.gateway.seckill.SeckillSignature;
import reactor.core.publisher.Mono;

@Component
public class SeckillSignFilter implements GlobalFilter, Ordered
{
    @Value("${seckill.sign-secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        ServerHttpRequest request = exchange.getRequest();
        if (!HttpMethod.POST.equals(request.getMethod()) || !"/tool/seckill/order".equals(request.getURI().getPath()))
        {
            return chain.filter(exchange);
        }
        String requestId = request.getHeaders().getFirst("X-Request-Id");
        String activityId = request.getHeaders().getFirst("X-Activity-Id");
        String timestamp = request.getHeaders().getFirst("X-Timestamp");
        String sign = request.getHeaders().getFirst("X-Sign");
        String userId = request.getHeaders().getFirst(SecurityConstants.DETAILS_USER_ID);
        if (StringUtils.isNotEmpty(userId))
        {
            userId = ServletUtils.urlDecode(userId);
        }
        try
        {
            long millis = timestamp == null ? 0L : Long.parseLong(timestamp);
            SeckillSignature.verify(requestId, activityId, userId, millis, sign, secret, System.currentTimeMillis());
        }
        catch (ServiceException ex)
        {
            return ServletUtils.webFluxResponseWriter(exchange.getResponse(), ex.getMessage(), HttpStatus.ERROR);
        }
        catch (NumberFormatException ex)
        {
            return ServletUtils.webFluxResponseWriter(exchange.getResponse(), SeckillSignature.BAD, HttpStatus.ERROR);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder()
    {
        return -199;
    }
}
