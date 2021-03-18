package com.appleframework.config;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import com.appleframework.config.core.EnvConfigurer;
import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.factory.BaseConfigurerFactory;
import com.appleframework.config.core.factory.ConfigurerFactory;
import com.appleframework.config.core.util.StringUtils;
import com.google.common.collect.ImmutableSortedSet;
import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;

public class PropertyConfigurerFactory extends BaseConfigurerFactory implements ConfigurerFactory {

	private static Logger logger = Logger.getLogger(PropertyConfigurerFactory.class);
	
	private static String KEY_DEPLOY_GROUP     = "deploy.group";
	private static String KEY_DEPLOY_DATAID    = "deploy.dataId";
	private static String KEY_DEPLOY_CONF_HOST = "deploy.confHost";
	
	public static String KEY_DEPLOY_ENV = "deploy.env";
	public static String KEY_ENV = "env";
	public static String KEY_DEFAULT_NAMESPACE = "default";
	
	private Map<String, DiamondManager> managerMap = new HashMap<>();
	
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
		
		String group = PropertyConfigurer.getString(KEY_DEPLOY_GROUP);
		String dataId = PropertyConfigurer.getString(KEY_DEPLOY_DATAID);
		
		String confHost = PropertyConfigurer.getString(KEY_DEPLOY_CONF_HOST);
		if (null != confHost) {
			com.taobao.diamond.common.Constants.DEFAULT_DOMAINNAME = confHost;
			com.taobao.diamond.common.Constants.DAILY_DOMAINNAME   = confHost;
		}

		if(null == group) {
			group = this.getDeployEnv();
		}
		if(null == dataId) {
			dataId = this.getApplicationName();
		}
			
		logger.warn("配置项：group=" + group);
		logger.warn("配置项：dataId=" + dataId);

		if (!StringUtils.isEmpty(group) && !StringUtils.isEmpty(dataId)) {
			addNamespace(dataId, 0);
		}
		
		for (String namespace : NAMESPACE_NAMES.values()) {
			initManager(group, namespace);
		}
	}	
	
	private void initManager(String group, String dataId) {
		
		ManagerListener springMamagerListener = new ManagerListener() {

			public Executor getExecutor() {
				return null;
			}

			public void receiveConfigInfo(String configInfo) {
				// 客户端处理数据的逻辑
				logger.warn("已改动的配置：\n" + configInfo);
				Map<String, Properties> map = new HashMap<>();
				try {
					Properties properties = new Properties();
					properties.load(new StringReader(configInfo));
						
					Set<String> propertyNames = properties.stringPropertyNames();
					for (String key : propertyNames) {
						String value = properties.getProperty(key, null);
						PropertyConfigurer.setPropertyAll(dataId, key, value);
					}
					map.put(dataId, properties);
				} catch (Exception e) {
					logger.error(e);
					return;
				}					
				//事件触发				
				notifyPropertiesChanged(map);
			}
		};
		
		DiamondManager manager = new DefaultDiamondManager(group, dataId, springMamagerListener);
		managerMap.put(dataId, manager);
	}
	
	public Map<String, Properties> getAllRemoteProperties() {
		if (!isLoadRemote() || managerMap.size() == 0) {
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
					DiamondManager manager = managerMap.get(namespace);
					String configInfo = manager.getAvailableConfigureInfomation(30000);
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
		
	@Override
	public Properties onLoadFinish(Properties properties) {
		setSystemProperty(properties);
		return properties;
	}

	private String getDeployEnv() {
		String env = System.getProperty(KEY_DEPLOY_ENV);
		if (StringUtils.isEmpty(env)) {
			env = System.getProperty(KEY_ENV);
			if (StringUtils.isEmpty(env)) {
				env = EnvConfigurer.env;
				if (StringUtils.isEmpty(env)) {
					env = PropertyConfigurer.getString(KEY_DEPLOY_ENV);
				}
			}
		}
		return env;
	}

	@Override
	public void close() {
		managerMap.entrySet().forEach(entry -> 
			entry.getValue().close()
		);
	}
	
	private String getApplicationName() {
		String appName = PropertyConfigurer.getString("spring.application.name");
		if(null == appName) {
			appName = PropertyConfigurer.getString("application.name");
		}
		return appName;
	}

	@Override
	public Properties getRemoteProperties(String namespace) {
		Map<String, Properties> map = this.getAllRemoteProperties();
		return map.get(namespace);
	}

	@Override
	public String getRemoteConfigInfo(String namespace) {
		return managerMap.get(namespace).getAvailableConfigureInfomation(30000);
	}	
	
}