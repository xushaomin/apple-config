package com.appleframework.config.core;

public class EnvConfigurer {
	
	public static String KEY_DEPLOY_ENV = "deploy.env";

	public static String env = null;
	
	public static String getEnv() {
		if(null == env) {
			env = System.getProperty(KEY_DEPLOY_ENV);
		}
		return env;
	}
	
	public static void setEnv(String env) {
		EnvConfigurer.env = env;
		System.setProperty(KEY_DEPLOY_ENV, env);
	}
}
