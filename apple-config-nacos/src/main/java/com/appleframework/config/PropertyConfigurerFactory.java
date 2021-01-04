package com.appleframework.config;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class PropertyConfigurerFactory extends BaseConfigurerFactory implements ConfigurerFactory {

	private static Logger logger = LoggerFactory.getLogger(PropertyConfigurerFactory.class);
	
	private static String KEY_DEPLOY_GROUP     = "deploy.group";
	private static String KEY_DEPLOY_DATAID    = "deploy.dataId";
	//private static String KEY_DEPLOY_CONF_HOST = "deploy.confHost";
	
	private ConfigService configService;
	
	private String group;
	private String dataId;
	
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
		
		group = PropertyConfigurer.getString(KEY_DEPLOY_GROUP);
		dataId = PropertyConfigurer.getString(KEY_DEPLOY_DATAID);
		
//		String confHost = PropertyConfigurer.getString(KEY_DEPLOY_CONF_HOST);
//		if (null != confHost) {
//			com.taobao.diamond.common.Constants.DEFAULT_DOMAINNAME = confHost;
//			com.taobao.diamond.common.Constants.DAILY_DOMAINNAME   = confHost;
//		}

		if(null == group) {
			group = this.getDeployEnv();
		}
		if(null == dataId) {
			dataId = this.getAppId();
		}
			
		logger.warn("配置项：group=" + group);
		logger.warn("配置项：dataId=" + dataId);
		
		Properties properties = new Properties();
        properties.put(PropertyKeyConst.ENDPOINT, "${endpoint}");
        properties.put(PropertyKeyConst.NAMESPACE, "${namespace}");


		if (!StringUtils.isEmpty(group) && !StringUtils.isEmpty(dataId)) {
			
			PropertiesListener springMamagerListener = new PropertiesListener() {

				@Override
	            public void innerReceive(Properties properties) {
					logger.warn("已改动的配置：\n" + properties);
					try {
						PropertyConfigurer.load(properties);
					} catch (Exception e) {
						logger.error(e.getMessage());
						return;
					}					
					//事件触发
					notifyPropertiesChanged(PropertyConfigurer.getProps());
	            }
			};
			try {
				configService = NacosFactory.createConfigService(properties);
				configService.addListener(dataId, group, springMamagerListener);
			} catch (NacosException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			String configInfo = configService.getConfig(dataId, group, 30000);
			logger.warn("配置项内容: \n" + configInfo);
			return configInfo;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public Properties getRemoteProperties(String namespace) {
		return this.getRemoteProperties();
	}

	@Override
	public Map<String, Properties> getAllRemoteProperties() {
		Properties props = this.getRemoteProperties(null);
		Map<String, Properties> propsMap = new HashMap<String, Properties>();
		propsMap.put(Constants.KEY_NAMESPACE, props);
		return propsMap;
	}
	
	private String getDeployEnv() {
		String env = System.getProperty(Constants.KEY_APP_ENV);
		if (StringUtils.isEmpty(env)) {
			env = PropertyConfigurer.getString(Constants.KEY_APP_ENV);
			if (StringUtils.isEmpty(env)) {
				env = EnvConfigurer.env;
			}
		}
		return env;
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