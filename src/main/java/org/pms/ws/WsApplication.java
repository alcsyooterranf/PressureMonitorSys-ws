package org.pms.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WebSocket服务启动类
 *
 * @author zeal
 * @version 1.0
 * @since 2025-11-25
 */
@Slf4j
@SpringBootApplication
public class WsApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(WsApplication.class, args);
		log.info("WebSocket服务启动成功！");
	}
	
}

