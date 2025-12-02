package org.pms.ws.handler;

import com.alibaba.fastjson2.JSON;
import com.pms.auth.core.utils.JwtUtil;
import com.pms.auth.starter.service.JwtService;
import com.pms.auth.starter.service.LoginUser;
import com.pms.types.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pms.ws.service.WebSocketSessionManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 告警WebSocket处理器
 * 处理WebSocket连接、消息和断开
 *
 * @author zeal
 * @version 1.0
 * @since 2025-11-25
 */
@Slf4j
@Component
public class AlertWebSocketHandler extends TextWebSocketHandler {
	
	private final JwtService jwtService;
	private final WebSocketSessionManager sessionManager;
	
	public AlertWebSocketHandler(JwtService jwtService, WebSocketSessionManager sessionManager) {
		this.jwtService = jwtService;
		this.sessionManager = sessionManager;
	}
	
	/**
	 * WebSocket连接建立后调用
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("WebSocket连接建立: sessionId={}", session.getId());
		
		try {
			// 1. 从query参数获取token
			String token = extractToken(session);
			if (StringUtils.isBlank(token)) {
				log.warn("WebSocket握手失败: token为空, sessionId={}", session.getId());
				session.close(CloseStatus.BAD_DATA.withReason("Token is required"));
				return;
			}
			
			// 2. 验证token
			JwtUtil.validateToken(token);
			
			// 3. 从token中获取用户信息
			LoginUser loginUser = jwtService.getLoginUserFromToken(token);
			Long userId = loginUser.getUserAggregate().getId();
			String username = loginUser.getUserAggregate().getUsername();
			
			// 4. 验证角色权限（只允许admin和operator）
			boolean hasPermission = loginUser.getAuthorities().stream()
					.map(GrantedAuthority::getAuthority)
					.anyMatch(role -> "ROLE_admin".equals(role) || "ROLE_operator".equals(role));
			
			if (!hasPermission) {
				log.warn("WebSocket握手失败: 用户{}没有权限, roles={}", username, loginUser.getAuthorities());
				session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Permission denied"));
				return;
			}
			
			// 5. 将session存入管理器
			sessionManager.addSession(userId, username, session);
			log.info("WebSocket连接成功: userId={}, username={}, sessionId={}", userId, username, session.getId());
			
			// 6. 发送欢迎消息
			Map<String, Object> welcomeMsg = new HashMap<>();
			welcomeMsg.put("type", "welcome");
			welcomeMsg.put("message", "WebSocket连接成功");
			welcomeMsg.put("userId", userId);
			welcomeMsg.put("username", username);
			session.sendMessage(new TextMessage(JSON.toJSONString(welcomeMsg)));
			
		} catch (Exception e) {
			log.error("WebSocket连接建立失败: sessionId={}, error={}", session.getId(), e.getMessage(), e);
			session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"));
		}
	}
	
	/**
	 * 接收到客户端消息时调用
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		log.debug("收到WebSocket消息: sessionId={}, message={}", session.getId(), payload);
		
		// 可以处理客户端发送的心跳消息等
		if ("ping".equals(payload)) {
			session.sendMessage(new TextMessage("pong"));
		}
	}
	
	/**
	 * WebSocket连接关闭后调用
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.info("WebSocket连接关闭: sessionId={}, status={}", session.getId(), status);
		sessionManager.removeSession(session.getId());
	}
	
	/**
	 * WebSocket传输错误时调用
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.error("WebSocket传输错误: sessionId={}, error={}", session.getId(), exception.getMessage(), exception);
		sessionManager.removeSession(session.getId());
		if (session.isOpen()) {
			session.close(CloseStatus.SERVER_ERROR);
		}
	}
	
	/**
	 * 从WebSocket握手请求的query参数中提取token
	 *
	 * @param session WebSocket会话
	 * @return token字符串
	 */
	private String extractToken(WebSocketSession session) {
		URI uri = session.getUri();
		if (uri == null) {
			return null;
		}
		
		// 解析query参数
		Map<String, String> queryParams = UriComponentsBuilder.fromUri(uri)
				.build()
				.getQueryParams()
				.toSingleValueMap();
		
		// 获取token参数
		String token = queryParams.get("token");
		if (StringUtils.isNotBlank(token)) {
			// 如果token带有Bearer前缀，去掉前缀
			if (token.startsWith(Constants.TOKEN_PREFIX)) {
				token = token.substring(Constants.TOKEN_PREFIX.length());
			}
		}
		
		return token;
	}
	
}

