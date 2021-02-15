package com.appleframework.config.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandOption {

	private static Logger logger = LoggerFactory.getLogger(CommandOption.class);

	public static void parser(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String envArgs = args[i];
			String[] envs = envArgs.split("=");
			if (envs.length == 2) {
				String key = envs[0];
				String value = envs[1];
				logger.warn("配置项：" + key + "=" + value);
				if (key.trim().toLowerCase().indexOf("env") > -1) {
					EnvConfigurer.setEnv(value);
					System.setProperty(key, value);
				}
			} else {
				logger.error("错误参数：" + envArgs);
			}
		}
	}
}
