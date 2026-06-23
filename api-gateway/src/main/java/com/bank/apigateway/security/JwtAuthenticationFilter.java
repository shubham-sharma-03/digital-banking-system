package com.bank.apigateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {


            String path = exchange.getRequest()
                    .getURI()
                    .getPath();

            System.out.println("PATH = " + path);

             // Public APIs
            if (path.startsWith("/api/auth")
                    || path.startsWith("/transactions")
                    || path.startsWith("/accounts")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            System.out.println("HEADER = " + authHeader);

            if (authHeader == null ||
                    !authHeader.startsWith("Bearer ")) {

                System.out.println("NO TOKEN");

                exchange.getResponse()
                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

//          System.out.println("TOKEN LENGTH = " + token.length());

          System.out.println("TOKEN = " + token);

            boolean valid = jwtUtil.validateToken(token);

            System.out.println("VALID = " + valid);

            if (!valid) {

                System.out.println("INVALID TOKEN");

                exchange.getResponse()
                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
    }
}