package com.appleframework.config;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.listener.impl.PropertiesListener;
import com.appleframework.config.core.Constants;
import com.appleframework.config.core.EnvConfigurer;
import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.factory.BaseConfigurerFactory;
import com.appleframework.config.core.factory.ConfigurerFactory;
import com.appleframework.config.core.util.StringUtils;
import com.google.common.collect.ImmutableSortedSet;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

public class PropertyConfigurerFactory extends BaseConfigurerFactory implements ConfigurerFactory {

	private static Log logger = LogFactory.get(PropertyConfigurerFactory.class);

	private static String KEY_DEPLOY_GROUP     = "deploy.group";
	private static String KEY_DEPLOY_DATAID    = "deploy.dataId";
	private static String KEY_DEPLOY_CONF_HOST = "deploy.confHost";
	private static String KEY_DEPLOY_ENV       = "deploy.env";
	
	private static String DEFAULT_DEPLOY_CONF_HOST = "config-nacos.appleframework.com";
	
	private ConfigService configService;
	
	private String group;
	private String dataId;
	private String confHost;
	
	public PropertyConfigurerFactory() {
		
	}
	
	public PropertyConfigurerFactory(Properties props) {
		convertLocalProperties(props);
	}
	
	public void init() {
		
		Version.logVersion();
				
		initSystemProperties();

		initEventListener();
		
		initDiamondManager();
	}
	
	private void initDiamondManager() {
		if (!isLoadRemote()) {
			return;
		}
		Properties properties = new Properties();
		
		group = PropertyConfigurer.getString(KEY_DEPLOY_GROUP);
		dataId = PropertyConfigurer.getString(KEY_DEPLOY_DATAID);
		confHost = getServerAddr();

		if(null == group) {
			group = this.getEnv();
		}
		if(null == dataId) {
			dataId = this.getAppId();
		}
		
		properties.put(PropertyKeyConst.SERVER_ADDR, confHost);
		
		logger.warn("配置项：group=" + group);
		logger.warn("配置项：dataId=" + dataId);
		logger.warn("配置项：confHost=" + confHost);
		
		if (!StringUtils.isEmpty(group) && !StringUtils.isEmpty(dataId)) {
			addNamespace(dataId, 0);
		}
		try {
			configService = NacosFactory.createConfigService(properties);
			for (String namespace : NAMESPACE_NAMES.values()) {
				PropertiesListener propertiesListener = new PropertiesListener() {
					@Override
					public void innerReceive(Properties properties) {
						logger.warn("已改动的配置：\n" + properties);
						Map<String, Properties> map = new HashMap<>();
						try {
							Set<String> propertyNames = properties.stringPropertyNames();
							for (String key : propertyNames) {
								String value = properties.getProperty(key, null);
								PropertyConfigurer.setPropertyAll(namespace, key, value);
							}
							map.put(namespace, properties);
						} catch (Exception e) {
							logger.error(e);
							return;
						}
						// 事件触发
						notifyPropertiesChanged(map);
					}
				};
				configService.addListener(namespace, group, propertiesListener);
			}
		} catch (NacosException e) {
			logger.error(e.getMessage());
		}
	}
	
	public Properties getRemoteProperties() {
		Properties properties = new Properties();
		if (!isLoadRemote() || null == configService) {
			return properties;
		}
		try {
			String configInfo = configService.getConfig(dataId, group, 30000);
			logger.warn("配置项内容: \n" + configInfo);
			if (!StringUtils.isEmpty(configInfo)) {
				properties.load(new StringReader(configInfo));
			} else {
				logger.error("在配置管理中心找不到配置信息");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return properties;
	}
	
	@Override
	public String getRemoteConfigInfo(String namespace) {
		if (!isLoadRemote() || null == configService) {
			return null;
		}
		try {
			String configInfo = configService.getConfig(namespace, group, 30000);
			logger.warn("配置项内容: \n" + configInfo);
			return configInfo;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public Properties getRemoteProperties(String namespace) {
		String configInfo = this.getRemoteConfigInfo(namespace);
		Properties properties = new Properties();
		if (!StringUtils.isEmpty(configInfo)) {
			try {
				properties.load(new StringReader(configInfo));
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		return properties;
	}

	@Override
	public Map<String, Properties> getAllRemoteProperties() {

		if (!isLoadRemote() || null == configService) {
			return null;
		}
		Map<String, Properties> map = new HashMap<>();
		try {
			
			// sort by order asc
			ImmutableSortedSet<Integer> orders = ImmutableSortedSet.copyOf(NAMESPACE_NAMES.keySet());
			Iterator<Integer> iterator = orders.iterator();

			while (iterator.hasNext()) {
				int order = iterator.next();
				for (String namespace : NAMESPACE_NAMES.get(order)) {
					String configInfo = configService.getConfig(namespace, group, 30000);
					Properties properties = new Properties();
					if (!StringUtils.isEmpty(configInfo)) {
						properties.load(new StringReader(configInfo));
						
						Set<String> propertyNames = properties.stringPropertyNames();
						if (propertyNames.size() > 0) {
							logger.info("The namespace [" + namespace + "] properties: ");
						} else {
							logger.info("The namespace [" + namespace + "] properties is null !!!");
						}
						
						for (String key : propertyNames) {
							String value = properties.getProperty(key, null);
							if (null != value) {
								logger.info("    " + key + "=" + value);
							}
						}
					}
					map.put(namespace, properties);
				}
			}			
		} catch (Exception e) {
			logger.error(e);
		}
		
		return map;
	
	}
	
	private String getEnv() {
		String env = System.getProperty(Constants.KEY_APP_ENV);
		if (StringUtils.isEmpty(env)) {
			env = System.getProperty(Constants.KEY_ENV);
			if (StringUtils.isEmpty(env)) {
				env = PropertyConfigurer.getString(Constants.KEY_APP_ENV);
				if (StringUtils.isEmpty(env)) {
					env = PropertyConfigurer.getString(Constants.KEY_ENV);
					if (StringUtils.isEmpty(env)) {
						env = PropertyConfigurer.getString(KEY_DEPLOY_ENV);
						if (StringUtils.isEmpty(env)) {
							if (StringUtils.isEmpty(EnvConfigurer.env)) {
								env = "dev";
								logger.warn("the default env is dev !!! ");
							} else {
								env = EnvConfigurer.env;
							}
						}
					}
				}
			}
		}
		return env;
	}
	
	private String getServerAddr() {
		String addr = System.getProperty(KEY_DEPLOY_CONF_HOST);
		if (StringUtils.isEmpty(addr)) {
			addr = PropertyConfigurer.getString(KEY_DEPLOY_CONF_HOST);
			if (StringUtils.isEmpty(addr)) {
				addr = DEFAULT_DEPLOY_CONF_HOST;
			}
		}
		return addr;
	}

	@Override
	public void close() {
		if(null != configService) {
			try {
				configService.shutDown();
			} catch (NacosException e) {
			}
		}
	}
	
	private String getAppId() {
		String appId = System.getProperty(Constants.KEY_APP_ID);
		if(null == appId) {
			appId = System.getProperty("spring.application.name");
			if(null == appId) {
				appId = PropertyConfigurer.getString(Constants.KEY_APP_ID);
				if(null == appId) {
					appId = PropertyConfigurer.getString("spring.application.name");
				}
			}
		}
		return appId;
	}
	
}