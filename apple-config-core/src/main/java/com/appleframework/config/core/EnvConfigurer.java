package com.appleframework.config.core;

public class EnvConfigurer {
	
	public static String KEY_APPLE_DEPLOY_ENV  = "deploy.env";
	public static String KEY_SPRING_DEPLOY_ENV = "spring.profiles.active";

	public static String env = null;
	
	public static String getEnv() {
		if(null == env) {
			env = System.getProperty(KEY_APPLE_DEPLOY_ENV);
			if(null == env) {
				env = System.getProperty(KEY_SPRING_DEPLOY_ENV);
			}
		}
		return env;
	}
	
	public static void setEnv(String env) {
		EnvConfigurer.env = env;
		System.setProperty(KEY_APPLE_DEPLOY_ENV, env);
		System.setProperty(KEY_SPRING_DEPLOY_ENV, env);
	}
}
