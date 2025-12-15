package org.pms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * WebSocket服务启动类
 *
 * @author zeal
 * @version 1.0
 * @since 2025-11-25
 */
@Slf4j
@SpringBootApplication
@EnableFeignClients(basePackages = "org.pms.trigger.feign")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		log.info("WebSocket服务启动成功！");
	}

}

