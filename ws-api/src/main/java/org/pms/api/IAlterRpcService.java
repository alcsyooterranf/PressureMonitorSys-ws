package org.pms.api;

import java.util.Map;

/**
 * @author alcsyooterranf
 * @program PressureMonitorSys-ws
 * @description WebSocket报警推送服务对外接口
 * @create 2025/12/17
 */
public interface IAlterRpcService {
	
	/**
	 * 广播告警消息给所有在线用户
	 *
	 * @param alertData 告警数据
	 * @return 响应结果
	 */
	Map<String, Object> broadcastAlert(Map<String, Object> alertData);
	
	/**
	 * 推送告警消息给指定用户
	 *
	 * @param userId    用户ID
	 * @param alertData 告警数据
	 * @return 响应结果
	 */
	Map<String, Object> pushAlertToUser(Long userId, Map<String, Object> alertData);
	
	/**
	 * 推送告警消息给指定用户列表
	 *
	 * @param request 请求参数（包含userIds和alertData）
	 * @return 响应结果
	 */
	Map<String, Object> pushAlertToUsers(Map<String, Object> request);
	
	/**
	 * 获取在线用户数
	 *
	 * @return 在线用户数
	 */
	Map<String, Object> getOnlineCount();
	
	/**
	 * 检查用户是否在线
	 *
	 * @param userId 用户ID
	 * @return 在线状态
	 */
	Map<String, Object> checkUserOnline(Long userId);
	
}
