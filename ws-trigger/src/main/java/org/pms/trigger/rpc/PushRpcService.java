package org.pms.trigger.rpc;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.pms.api.IPushRpcService;
import org.pms.core.service.WebSocketSessionManager;
import org.pms.types.Response;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息主动推送服务
 * 提供RPC接口供business服务调用
 *
 * @author alcsyooterranf
 * @version 1.0
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/rpc/ws")
public class PushRpcService implements IPushRpcService {
	
	@Resource
	private WebSocketSessionManager sessionManager;
	
	/**
	 * 广播消息给所有在线用户
	 *
	 * @param data 数据
	 * @return 响应结果
	 */
	@PostMapping("/broadcast")
	public Response<Void> broadcast(@RequestBody Map<String, Object> data) {
		log.info("收到广播数据: data={}", data);
		
		int successCount = 0;
		int failCount = 0;
		
		for (WebSocketSessionManager.SessionInfo sessionInfo : sessionManager.getAllSessions()) {
			try {
				sendToSession(sessionInfo.getSession(), data);
				successCount++;
			} catch (Exception e) {
				log.error("发送消息失败: userId={}, username={}, sessionId={}, error={}",
						sessionInfo.getUserId(), sessionInfo.getUsername(),
						sessionInfo.getSession().getId(), e.getMessage());
				failCount++;
			}
		}
		log.info("消息广播完成: 成功={}, 失败={}, 总数={}", successCount, failCount, successCount + failCount);
		return Response.<Void>builder()
				.message(String.format("消息广播完成: 成功=%d, 失败=%d, 总数=%d", successCount, failCount, successCount + failCount))
				.build();
	}
	
	/**
	 * 推送消息给指定用户列表
	 *
	 * @param userIds 用户ID列表
	 * @return 响应结果
	 */
	@Override
	@PostMapping("/push/batch")
	public Response<Void> pushToUsers(Long[] userIds, @RequestBody Map<String, Object> data) {
		log.info("推送消息给指定用户列表: userIds={}, alertData={}", userIds, data);
		
		int successCount = 0;
		for (Long userId : userIds) {
			if (pushToUser(userId, data)) {
				successCount++;
			}
		}
		log.info("消息推送完成: 成功={}, 总数={}", successCount, userIds.length);
		return Response.<Void>builder()
				.message(String.format("消息推送完成: 成功=%d, 总数=%d", successCount, userIds.length))
				.build();
	}
	
	/**
	 * 推送消息给指定用户
	 *
	 * @param userId 用户ID
	 * @param data   数据
	 * @return 响应结果
	 */
	private Boolean pushToUser(@PathVariable Long userId, @RequestBody Map<String, Object> data) {
		WebSocketSession session = sessionManager.getSessionByUserId(userId);
		if (session == null || !session.isOpen()) {
			log.warn("用户不在线，无法推送消息: userId={}", userId);
			return false;
		}
		
		try {
			sendToSession(session, data);
			log.info("消息推送成功: userId={}", userId);
			return true;
		} catch (Exception e) {
			log.error("推送消息失败: userId={}, error={}", userId, e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 发送消息到WebSocket会话
	 *
	 * @param session WebSocket会话
	 * @param data    数据
	 * @throws IOException 发送失败时抛出异常
	 */
	private void sendToSession(WebSocketSession session, Map<String, Object> data) throws IOException {
		// 构造消息格式
		Map<String, Object> message = new HashMap<>();
		message.put("type", "alert");
		message.put("timestamp", System.currentTimeMillis());
		message.put("data", data);
		
		// 发送消息
		String jsonMessage = JSON.toJSONString(message);
		session.sendMessage(new TextMessage(jsonMessage));
	}
	
}

