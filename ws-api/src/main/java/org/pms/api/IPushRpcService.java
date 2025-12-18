package org.pms.api;

import org.pms.types.Response;

import java.util.Map;

/**
 * @author alcsyooterranf
 * @program PressureMonitorSys-ws
 * @description WebSocket报警推送服务对外接口
 * @create 2025/12/17
 */
public interface IPushRpcService {
	
	/**
	 * 广播告警消息给所有在线用户
	 *
	 * @param alertData 告警数据
	 * @return 响应结果
	 */
	Response<Void> broadcast(Map<String, Object> alertData);
	
	/**
	 * 推送告警消息给指定用户列表
	 *
	 * @param request 请求参数（包含userIds和alertData）
	 * @return 响应结果
	 */
	Response<Void> pushToUsers(Long[] userIds, Map<String, Object> request);
	
}
