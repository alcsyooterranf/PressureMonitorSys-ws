package org.pms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置
 * WS-Service的REST接口是内部RPC接口，允许匿名访问
 * WebSocket连接通过JWT token验证，在WebSocketHandler中处理
 *
 * @author zeal
 * @version 1.0
 * @since 2025-11-25
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（因为是内部RPC服务）
                .csrf(AbstractHttpConfigurer::disable)
                // 允许所有请求匿名访问
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}

