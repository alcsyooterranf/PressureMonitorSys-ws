package org.pms.core.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 * 使用内存Map管理WebSocket会话（不依赖Redis）*
 * 注意：这是单机版本，如果需要支持分布式部署，需要改用Redis存储
 *
 * @author zeal
 * @version 1.0
 * @since 2025-11-25
 */
@Slf4j
@Service
public class WebSocketSessionManager {

    /**
     * 会话信息
     */
    @Data
    public static class SessionInfo {
        private Long userId;
        private String username;
        private WebSocketSession session;
        private Long connectTime;

        public SessionInfo(Long userId, String username, WebSocketSession session) {
            this.userId = userId;
            this.username = username;
            this.session = session;
            this.connectTime = System.currentTimeMillis();
        }
    }

    // key: sessionId, value: SessionInfo
    private final ConcurrentHashMap<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();

    // key: userId, value: sessionId（用于根据userId查找session）
    private final ConcurrentHashMap<Long, String> userSessionMap = new ConcurrentHashMap<>();

    /**
     * 添加会话
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param session  WebSocket会话
     */
    public void addSession(Long userId, String username, WebSocketSession session) {
        String sessionId = session.getId();

        // 如果用户已经有连接，先关闭旧连接
        String oldSessionId = userSessionMap.get(userId);
        if (oldSessionId != null) {
            SessionInfo oldSessionInfo = sessionMap.get(oldSessionId);
            if (oldSessionInfo != null && oldSessionInfo.getSession().isOpen()) {
                try {
                    log.info("用户{}已有连接，关闭旧连接: oldSessionId={}", username, oldSessionId);
                    oldSessionInfo.getSession().close();
                } catch (Exception e) {
                    log.error("关闭旧连接失败: sessionId={}, error={}", oldSessionId, e.getMessage());
                }
            }
            sessionMap.remove(oldSessionId);
        }

        // 添加新连接
        SessionInfo sessionInfo = new SessionInfo(userId, username, session);
        sessionMap.put(sessionId, sessionInfo);
        userSessionMap.put(userId, sessionId);

        log.info("添加WebSocket会话: userId={}, username={}, sessionId={}, 当前在线人数={}",
                userId, username, sessionId, sessionMap.size());
    }

    /**
     * 移除会话
     *
     * @param sessionId 会话ID
     */
    public void removeSession(String sessionId) {
        SessionInfo sessionInfo = sessionMap.remove(sessionId);
        if (sessionInfo != null) {
            userSessionMap.remove(sessionInfo.getUserId());
            log.info("移除WebSocket会话: userId={}, username={}, sessionId={}, 当前在线人数={}",
                    sessionInfo.getUserId(), sessionInfo.getUsername(), sessionId, sessionMap.size());
        }
    }

    /**
     * 根据用户ID获取会话
     *
     * @param userId 用户ID
     * @return WebSocket会话，如果不存在返回null
     */
    public WebSocketSession getSessionByUserId(Long userId) {
        String sessionId = userSessionMap.get(userId);
        if (sessionId == null) {
            return null;
        }
        SessionInfo sessionInfo = sessionMap.get(sessionId);
        return sessionInfo != null ? sessionInfo.getSession() : null;
    }

    /**
     * 根据sessionId获取会话信息
     *
     * @param sessionId 会话ID
     * @return 会话信息，如果不存在返回null
     */
    public SessionInfo getSessionInfo(String sessionId) {
        return sessionMap.get(sessionId);
    }

    /**
     * 获取所有会话
     *
     * @return 所有会话信息
     */
    public Collection<SessionInfo> getAllSessions() {
        return sessionMap.values();
    }

    /**
     * 获取在线用户数
     *
     * @return 在线用户数
     */
    public int getOnlineCount() {
        return sessionMap.size();
    }

    /**
     * 判断用户是否在线
     *
     * @param userId 用户ID
     * @return true-在线，false-离线
     */
    public boolean isOnline(Long userId) {
        String sessionId = userSessionMap.get(userId);
        if (sessionId == null) {
            return false;
        }
        SessionInfo sessionInfo = sessionMap.get(sessionId);
        return sessionInfo != null && sessionInfo.getSession().isOpen();
    }
}

