package org.pms.ws.config;

import lombok.extern.slf4j.Slf4j;
import org.pms.ws.handler.AlertWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 注册WebSocket处理器和路径
 *
 * @author zeal
 * @version 1.0
 * @since 2025-11-25
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AlertWebSocketHandler alertWebSocketHandler;

    public WebSocketConfig(AlertWebSocketHandler alertWebSocketHandler) {
        this.alertWebSocketHandler = alertWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(alertWebSocketHandler, "/ws/alert")
                .setAllowedOrigins("*"); // 允许所有来源（生产环境应该配置具体的域名）
        log.info("WebSocket handler registered: /ws/alert");
    }
}

