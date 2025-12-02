package org.pms.ws.controller;

import lombok.extern.slf4j.Slf4j;
import org.pms.ws.service.AlertPushService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 告警推送控制器
 * 提供RPC接口供backend服务调用
 *
 * @author zeal
 * @version 1.0
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/api/alert")
public class AlertPushController {

    private final AlertPushService alertPushService;

    public AlertPushController(AlertPushService alertPushService) {
        this.alertPushService = alertPushService;
    }

    /**
     * 广播告警消息给所有在线用户
     *
     * @param alertData 告警数据
     * @return 响应结果
     */
    @PostMapping("/broadcast")
    public Map<String, Object> broadcastAlert(@RequestBody Map<String, Object> alertData) {
        log.info("收到广播告警请求: alertData={}", alertData);

        try {
            alertPushService.broadcastAlert(alertData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "告警消息广播成功");
            response.put("onlineCount", alertPushService.getOnlineCount());
            return response;

        } catch (Exception e) {
            log.error("广播告警消息失败: error={}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "告警消息广播失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 推送告警消息给指定用户
     *
     * @param userId    用户ID
     * @param alertData 告警数据
     * @return 响应结果
     */
    @PostMapping("/push/{userId}")
    public Map<String, Object> pushAlertToUser(@PathVariable Long userId,
                                                @RequestBody Map<String, Object> alertData) {
        log.info("收到推送告警请求: userId={}, alertData={}", userId, alertData);

        try {
            boolean success = alertPushService.pushAlertToUser(userId, alertData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "告警消息推送成功" : "用户不在线或推送失败");
            response.put("userId", userId);
            response.put("online", alertPushService.isUserOnline(userId));
            return response;

        } catch (Exception e) {
            log.error("推送告警消息失败: userId={}, error={}", userId, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "告警消息推送失败: " + e.getMessage());
            response.put("userId", userId);
            return response;
        }
    }

    /**
     * 推送告警消息给指定用户列表
     *
     * @param request 请求参数（包含userIds和alertData）
     * @return 响应结果
     */
    @PostMapping("/push/batch")
    public Map<String, Object> pushAlertToUsers(@RequestBody Map<String, Object> request) {
        log.info("收到批量推送告警请求: request={}", request);

        try {
            Long[] userIds = ((java.util.List<Integer>) request.get("userIds"))
                    .stream()
                    .map(Long::valueOf)
                    .toArray(Long[]::new);
            Map<String, Object> alertData = (Map<String, Object>) request.get("alertData");

            int successCount = alertPushService.pushAlertToUsers(userIds, alertData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "告警消息批量推送完成");
            response.put("totalCount", userIds.length);
            response.put("successCount", successCount);
            response.put("failCount", userIds.length - successCount);
            return response;

        } catch (Exception e) {
            log.error("批量推送告警消息失败: error={}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "告警消息批量推送失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取在线用户数
     *
     * @return 在线用户数
     */
    @GetMapping("/online/count")
    public Map<String, Object> getOnlineCount() {
        int count = alertPushService.getOnlineCount();
        log.debug("查询在线用户数: count={}", count);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("onlineCount", count);
        return response;
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 在线状态
     */
    @GetMapping("/online/check/{userId}")
    public Map<String, Object> checkUserOnline(@PathVariable Long userId) {
        boolean online = alertPushService.isUserOnline(userId);
        log.debug("检查用户在线状态: userId={}, online={}", userId, online);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("online", online);
        return response;
    }
}

