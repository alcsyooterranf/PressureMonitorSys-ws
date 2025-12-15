package org.pms.core.service;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 告警推送服务
 * 负责将告警消息推送给WebSocket客户端
 *
 * @author zeal
 * @version 1.0
 * @since 2025-11-25
 */
@Slf4j
@Service
public class AlertPushService {
	
	@Resource
	private WebSocketSessionManager sessionManager;
	
	/**
	 * 广播告警消息给所有在线用户
	 *
	 * @param alertData 告警数据
	 */
	public void broadcastAlert(Map<String, Object> alertData) {
		log.info("广播告警消息: alertData={}", alertData);
		
		int successCount = 0;
		int failCount = 0;
		
		for (WebSocketSessionManager.SessionInfo sessionInfo : sessionManager.getAllSessions()) {
			try {
				sendAlertToSession(sessionInfo.getSession(), alertData);
				successCount++;
			} catch (Exception e) {
				log.error("发送告警消息失败: userId={}, username={}, sessionId={}, error={}",
						sessionInfo.getUserId(), sessionInfo.getUsername(),
						sessionInfo.getSession().getId(), e.getMessage());
				failCount++;
			}
		}
		
		log.info("告警消息广播完成: 成功={}, 失败={}, 总数={}", successCount, failCount, successCount + failCount);
	}
	
	/**
	 * 推送告警消息给指定用户
	 *
	 * @param userId    用户ID
	 * @param alertData 告警数据
	 * @return true-推送成功，false-推送失败（用户不在线或发送失败）
	 */
	public boolean pushAlertToUser(Long userId, Map<String, Object> alertData) {
		log.info("推送告警消息给用户: userId={}, alertData={}", userId, alertData);
		
		WebSocketSession session = sessionManager.getSessionByUserId(userId);
		if (session == null || !session.isOpen()) {
			log.warn("用户不在线，无法推送告警: userId={}", userId);
			return false;
		}
		
		try {
			sendAlertToSession(session, alertData);
			log.info("告警消息推送成功: userId={}", userId);
			return true;
		} catch (Exception e) {
			log.error("推送告警消息失败: userId={}, error={}", userId, e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 推送告警消息给指定用户列表
	 *
	 * @param userIds   用户ID列表
	 * @param alertData 告警数据
	 * @return 推送成功的用户数
	 */
	public int pushAlertToUsers(Long[] userIds, Map<String, Object> alertData) {
		log.info("推送告警消息给指定用户: userIds={}, alertData={}", userIds, alertData);
		
		int successCount = 0;
		for (Long userId : userIds) {
			if (pushAlertToUser(userId, alertData)) {
				successCount++;
			}
		}
		
		log.info("告警消息推送完成: 成功={}, 总数={}", successCount, userIds.length);
		return successCount;
	}
	
	/**
	 * 发送告警消息到WebSocket会话
	 *
	 * @param session   WebSocket会话
	 * @param alertData 告警数据
	 * @throws IOException 发送失败时抛出异常
	 */
	private void sendAlertToSession(WebSocketSession session, Map<String, Object> alertData) throws IOException {
		// 构造消息格式
		Map<String, Object> message = new HashMap<>();
		message.put("type", "alert");
		message.put("timestamp", System.currentTimeMillis());
		message.put("data", alertData);
		
		// 发送消息
		String jsonMessage = JSON.toJSONString(message);
		session.sendMessage(new TextMessage(jsonMessage));
		
		log.debug("告警消息已发送: sessionId={}, message={}", session.getId(), jsonMessage);
	}
	
	/**
	 * 获取在线用户数
	 *
	 * @return 在线用户数
	 */
	public int getOnlineCount() {
		return sessionManager.getOnlineCount();
	}
	
	/**
	 * 判断用户是否在线
	 *
	 * @param userId 用户ID
	 * @return true-在线，false-离线
	 */
	public boolean isUserOnline(Long userId) {
		return sessionManager.isOnline(userId);
	}
	
}

