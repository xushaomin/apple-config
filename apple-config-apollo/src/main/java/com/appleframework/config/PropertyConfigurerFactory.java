package com.appleframework.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appleframework.config.core.Constants;
import com.appleframework.config.core.EnvConfigurer;
import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.factory.BaseConfigurerFactory;
import com.appleframework.config.core.factory.ConfigurerFactory;
import com.appleframework.config.core.util.StringUtils;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;

public class PropertyConfigurerFactory extends BaseConfigurerFactory implements ConfigurerFactory {

	private static Logger logger = LoggerFactory.getLogger(PropertyConfigurerFactory.class);
	
	private static String KEY_DEPLOY_APP_ID    = "app.id";
	private static String KEY_DEPLOY_META_URL  = "apollo.meta";
	
	private Map<String, Config> configMap = new HashMap<String, Config>();
		
	public PropertyConfigurerFactory() {
		
	}
	
	public PropertyConfigurerFactory(Properties props) {
		convertLocalProperties(props);
	}
	
	public void init() {
		
		Version.logVersion();
				
		initSystemProperties();

		initEventListener();
		
		initApolloConfig();
	}
	
	private void initApolloConfig() {
		
		if (!isLoadRemote()) {
			return;
		}
		logger.warn("配置项：appName=" + this.getApplicationName());
		String appId = this.getDeployAppId();
		if(null == appId) {
			appId = System.getProperty(KEY_DEPLOY_APP_ID);
		}
		else {
			System.setProperty("app.id", appId);
		}
		logger.warn("配置项：appId=" + appId);
		
		String env = this.getDeployEnv();
		logger.warn("配置项：env=" + env);
		
		String meta = getDeployMeta();
		logger.warn("配置项：meta=" + meta);
		
		String refreshInterval = this.getRefreshInterval();
		if(null == refreshInterval) {
			System.setProperty("apollo.refreshInterval", "1");
		}
		
		String namespaces = this.getDeployNamespaces();
		
		ConfigChangeListener changeListener = new ConfigChangeListener() {
			@Override
			public void onChange(ConfigChangeEvent changeEvent) {
		        logger.info("Changes for namespace {}", changeEvent.getNamespace());
		        logger.warn("已改动的配置：");
		        for (String key : changeEvent.changedKeys()) {
		        	ConfigChange change = changeEvent.getChange(key);
		        	logger.info("Change - key: {}, oldValue: {}, newValue: {}, changeType: {}",
		        			change.getPropertyName(), change.getOldValue(), change.getNewValue(),
		        			change.getChangeType());
		        	// 客户端处理数据的逻辑
					System.out.println(change.getPropertyName() + "=" + change.getNewValue());
					try {
						PropertyConfigurer.setProperty(change.getPropertyName(), change.getNewValue());
						PropertyConfigurer.setProperty(change.getNamespace() + "." + change.getPropertyName(), change.getNewValue());
						PropertyConfigurer.setProperty(change.getNamespace(), change.getPropertyName(), change.getNewValue());
					} catch (Exception e) {
						logger.error(e.getMessage());
						return;
					}					
					//事件触发
					notifyPropertiesChanged(PropertyConfigurer.getPropsMap());
		        }
			}
		};
		Config applicationConfig = ConfigService.getAppConfig();
		applicationConfig.addChangeListener(changeListener);
		configMap.put("application", applicationConfig);
		
		if (!StringUtils.isEmpty(namespaces)) {
			String[] namespaceArr = namespaces.trim().split(",");
			for (String namespace : namespaceArr) {
				if(!namespace.equals("application")) {
					Config namespaceConfig = ConfigService.getConfig(namespace);
					namespaceConfig.addChangeListener(changeListener);
					configMap.put(namespace, namespaceConfig);
				}
			}
		}
	}
	
	public Properties getRemoteProperties(String namespace) {
		Properties properties = new Properties();
		Config config = ConfigService.getConfig(namespace);
		for (String key : config.getPropertyNames()) {
			String value = config.getProperty(key, null);
			if(null != value) {
				properties.put(key, value);
			}
		}
		return properties;
	}
	
	@Override
	public Map<String, Properties> getAllRemoteProperties() {
		Map<String, Properties> propsMap = new HashMap<String, Properties>();
		if (!isLoadRemote() || configMap.size() == 0) {
			return propsMap;
		}
		try {
			for (Map.Entry<String, Config> map : configMap.entrySet()) {
		        String namespace = map.getKey();
		        logger.warn("命名空间" + namespace + "配置项内容: ");
		        Config config = map.getValue();
		        Set<String> propertyNames = config.getPropertyNames();
		        Properties properties = new Properties();
				for (String key : propertyNames) {
					String value = config.getProperty(key, null);
					if(null != value) {
						properties.put(key, value);
						System.out.println(key + "=" + value);
					}
				}
				propsMap.put(namespace, properties);
				System.out.println();
		    }
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return propsMap;
	}
	
	@Override
	public String getRemoteConfigInfo(String namespace) {
		return null;
	}

	private String getDeployEnv() {
		String env = System.getProperty(Constants.KEY_ENV);
		if (StringUtils.isEmpty(env)) {
			env = EnvConfigurer.env;
			if (StringUtils.isEmpty(env)) {
				env = PropertyConfigurer.getString(Constants.KEY_ENV);
			}
		}
		return env;
	}
	
	private String getDeployAppId() {
		return PropertyConfigurer.getString(KEY_DEPLOY_APP_ID);
	}
	
	private String getDeployMeta() {
		String meta = System.getProperty(KEY_DEPLOY_META_URL);
		if (StringUtils.isEmpty(meta)) {
			meta = PropertyConfigurer.getString(KEY_DEPLOY_META_URL);
		}
		return meta;
	}
	
	private String getDeployNamespaces() {
		String key = "apollo.bootstrap.namespaces";
		String namespaces = System.getProperty(key);
		if (StringUtils.isEmpty(namespaces)) {
			namespaces = PropertyConfigurer.getString(key);
		}
		return namespaces;
	}
	private String getRefreshInterval() {
		String key = "apollo.refreshInterval";
		String refreshInterval = System.getProperty(key);
		if (StringUtils.isEmpty(refreshInterval)) {
			refreshInterval = PropertyConfigurer.getString(key);
		}
		return refreshInterval;
	}

	@Override
	public void close() {
	}
	
	private String getApplicationName() {
		String appName = PropertyConfigurer.getString("spring.application.name");
		if(null == appName) {
			appName = PropertyConfigurer.getString("application.name");
		}
		return appName;
	}
	
}