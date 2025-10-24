package com.smartvn.api_gateway.filter;

import com.smartvn.api_gateway.config.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    @Autowired
    private JwtUtils jwtUtils;

    public AuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Bỏ qua xác thực cho các đường dẫn public
            if (isPublicPath(request)) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String jwt = authHeader.substring(7);

            try {
                if (!jwtUtils.validateToken(jwt)) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String userId = jwtUtils.getUserIdFromToken(jwt);
                ServerHttpRequest newRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .build();

                return chain.filter(exchange.mutate().request(newRequest).build());
            } catch (Exception e) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    private boolean isPublicPath(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // ✅ 1. Auth endpoints
        if (path.startsWith("/api/v1/auth/")) {
            return true;
        }

        // ✅ 2. OAuth2 endpoints
        if (path.startsWith("/oauth2/*") || path.startsWith("/login/oauth2/*")) {
            return true;
        }

        // ✅ 3. VNPay Callback - QUAN TRỌNG!
        // VNPay gọi callback từ server của họ, không có JWT
        if (path.equals("/api/v1/payment/vnpay-callback") && HttpMethod.GET.equals(method)) {
            return true;
        }

        // ✅ 4. Create multiple products (admin tool)
        if (path.equals("/api/v1/products/create-multiple") && HttpMethod.POST.equals(method)) {
            return true;
        }

        // ✅ 5. Products, categories, reviews - GET only
        if (path.startsWith("/api/v1/products") && HttpMethod.GET.equals(method)) {
            return true;
        }

        if (path.startsWith("/api/v1/categories") && HttpMethod.GET.equals(method)) {
            return true;
        }

        if (path.startsWith("/api/v1/reviews") && HttpMethod.GET.equals(method)) {
            return true;
        }

        // ✅ 6. Health check
        if (path.startsWith("/actuator/health")) {
            return true;
        }

        // ✅ THÊM: Admin endpoints yêu cầu JWT
        if (path.startsWith("/api/v1/admin/")) {
            return false; // Cần JWT token
        }

        return false;
    }

    public static class Config {
        // Cấu hình (nếu cần)
    }
}