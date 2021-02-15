package com.appleframework.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.ImmutableSortedSet;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.factory.BaseConfigurerFactory;
import com.appleframework.config.core.factory.ConfigurerFactory;
import com.appleframework.config.core.spi.NamespaceLoadSpi;
import com.appleframework.config.core.Constants;

public class PropertyConfigurerFactory extends BaseConfigurerFactory implements ConfigurerFactory {

	private static Logger logger = LoggerFactory.getLogger(PropertyConfigurerFactory.class);
	
	private static String KEY_DEPLOY_META_URL       = "apollo.meta";
	private static String KEY_DEPLOY_CONFIG_SERVICE = "apollo.configService";
	private static String KEY_DEPLOY_NAMESPACES     = "apollo.bootstrap.namespaces";
	//private static String KEY_DEPLOY_REFRESH_INT    = "apollo.refreshInterval";
	private static String KEY_NAMESPACE_APPLICATION = "application";
	
	private static String KEY_DELETEED = "PropertyChangeType.DELETED";
			
	private ConfigChangeListener changeListener;
	
	public PropertyConfigurerFactory() {
		
	}
	
	public PropertyConfigurerFactory(Properties props) {
		convertLocalProperties(props);
	}
	
	public void init() {
						
		initSystemProperties();

		initEventListener();
		
		initApolloConfig();
	}
	
	private void initMeta() {
		// apollo.meta or apollo.configService
		String meta = System.getProperty(KEY_DEPLOY_META_URL);
		if (null == meta) {
			meta = System.getProperty(KEY_DEPLOY_CONFIG_SERVICE);
			if (null == meta) {
				meta = PropertyConfigurer.getString(KEY_DEPLOY_META_URL);
				if (null == meta) {
					meta = PropertyConfigurer.getString(KEY_DEPLOY_CONFIG_SERVICE);
					if (null == meta) {
						logger.warn("apollo.meta or apollo.configService is not set on this project");
					} else {
						this.setDeployConfigService(meta);
						logger.info("apollo.configService = " + meta);
					}
				} else {
					logger.info("apollo.meta = " + meta);
					this.setDeployMeta(meta);
				}
			} else {
				logger.info("apollo.configService = " + meta);
				this.setDeployConfigService(meta);
			}
		} else {
			logger.info("apollo.meta = " + meta);
			this.setDeployMeta(meta);
		}
	}
	
	private void initAppId() {
		// app.id
		String appId = getDeployAppId();
		if (null == appId) {
			logger.warn("apollo.app.id is not set on this project");
		} else {
			this.setDeployAppId(appId);
			logger.info("apollo.app.id = " + appId);
		}
	}
	
	private void initEnv() {
		// env
		String env = this.getDeployEnv();
		if (null == env) {
			env = "dev";
			logger.warn("apollo env is not set on this project, use default env=dev");
		}
		this.setApolloEnv(env);
		logger.info("env = " + env);
	}
	
	private void initApolloConfig() {
		
		if (!isLoadRemote()) {
			return;
		}
		
		//app.id
		initAppId();

		//env
		initEnv();

		//apollo.meta or apollo.configService
		initMeta();
				
		addNamespace(KEY_NAMESPACE_APPLICATION, 0);

		int order = 1;
		String namespaces = this.getDeployNamespaces();
		if (StringUtils.isNotBlank(namespaces)) {
			String[] namespaceArr = namespaces.trim().split(",");
			for (String namespace : namespaceArr) {
				addNamespace(namespace.trim(), order++);
			}
		}
		
		ServiceLoader<NamespaceLoadSpi> serviceLoader = ServiceLoader.load(NamespaceLoadSpi.class);
		Iterator<NamespaceLoadSpi> iterator = serviceLoader.iterator();
		while (iterator.hasNext()) {
			NamespaceLoadSpi namespaceLoadSpi = iterator.next();
			Set<String> set = namespaceLoadSpi.load();
			addNamespaces(set, order++);
		}
		
		changeListener = new ConfigChangeListener() {
			@Override
			public void onChange(ConfigChangeEvent changeEvent) {
	        	logger.info("Change namespace " + changeEvent.getNamespace() + " properties: ");
	        	Map<String, Properties> propsMap = new HashMap<String, Properties>();
	        	String deletedKey = KEY_DELETEED;
	        	
		        for (String key : changeEvent.changedKeys()) {
		        	ConfigChange change = changeEvent.getChange(key);
		        	logger.info("Change - key: {}, oldValue: {}, newValue: {}, changeType: {}",
		        			change.getPropertyName(), change.getOldValue(), change.getNewValue(),
		        			change.getChangeType());
					String namespace = change.getNamespace().trim();
					String propertyName = change.getPropertyName().trim();

					try {
						if (PropertyChangeType.DELETED.equals(change.getChangeType())) {

							PropertyConfigurer.removePropertyAll(namespace, key);
							
							if(null == propsMap.get(deletedKey)) {
								Properties props = new Properties();
								props.put(namespace, propertyName);
								propsMap.put(deletedKey, props);
							}
							else {
								Properties props = propsMap.get(deletedKey);
								props.put(namespace, propertyName);
								propsMap.put(deletedKey, props);
							}

						}
						else {
							
							String newValue = change.getNewValue();
							PropertyConfigurer.setPropertyAll(namespace, propertyName, newValue);
							
							if(null == propsMap.get(namespace)) {
								Properties props = new Properties();
								props.put(propertyName, newValue);
								propsMap.put(namespace, props);
							}
							else {
								Properties props = propsMap.get(namespace);
								props.put(propertyName, newValue);
								propsMap.put(namespace, props);
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
						return;
					}			
		        }
		        if(!propsMap.isEmpty()) {
					//event notify
					notifyPropertiesChanged(propsMap);
		        }
			}
		};
	}
	
	public Properties getRemoteProperties(String namespace) {
		Properties properties = new Properties();
		Config config = ConfigService.getConfig(namespace);
		for (String key : config.getPropertyNames()) {
			String value = config.getProperty(key, null);
			if(null != value) {
				properties.put(key.trim(), value);
			}
		}
		return properties;
	}
	
	@Override
	public Map<String, Properties> getAllRemoteProperties() {
		Map<String, Properties> propsMap = new HashMap<String, Properties>();
		if (!isLoadRemote() || NAMESPACE_NAMES.size() == 0) {
			return propsMap;
		}
		try {
			// sort by order asc
			ImmutableSortedSet<Integer> orders = ImmutableSortedSet.copyOf(NAMESPACE_NAMES.keySet());
			Iterator<Integer> iterator = orders.iterator();

			while (iterator.hasNext()) {
				int order = iterator.next();
				for (String namespace : NAMESPACE_NAMES.get(order)) {
					Config config = ConfigService.getConfig(namespace);
					config.addChangeListener(changeListener);

					Set<String> propertyNames = config.getPropertyNames();
					if (propertyNames.size() > 0) {
						logger.info("The namespace [" + namespace + "] properties: ");
					} else {
						logger.warn("The namespace [" + namespace + "] properties is null !!!");
					}
					Properties properties = new Properties();
					for (String key : propertyNames) {
						String value = config.getProperty(key, null);
						if (null != value) {
							properties.put(key.trim(), value);
							logger.info("    " + key + "=" + value);
						}
					}
					propsMap.put(namespace, properties);
					logger.info("======================================================================");
				}
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
		String env = System.getProperty(Constants.KEY_APP_ENV);
		if(null == env) {
			env = System.getProperty(Constants.KEY_ENV);
			if(null == env) {
				env = PropertyConfigurer.getString(Constants.KEY_APP_ENV);
				if(null == env) {
					env = PropertyConfigurer.getString(Constants.KEY_ENV);
				}
			}
		}
		return env;
		
	}
		
	private String getDeployNamespaces() {
		String namespaces = System.getProperty(KEY_DEPLOY_NAMESPACES);
		if(null == namespaces) {
			namespaces = PropertyConfigurer.getString(KEY_DEPLOY_NAMESPACES);
		}
		return namespaces;
	}
	
	private String getDeployAppId() {
		String appId = System.getProperty(Constants.KEY_APP_ID);
		if(null == appId) {
			appId = PropertyConfigurer.getString(Constants.KEY_APP_ID);
		}
		return appId;
	}
		
//	private String getRefreshInterval() {
//		return System.getProperty(KEY_DEPLOY_REFRESH_INT);
//	}
	
	//set
	private void setApolloEnv(String env) {
		if(StringUtils.isNotBlank(env)) {
			System.setProperty(Constants.KEY_ENV, env);
			System.setProperty(Constants.KEY_APP_ENV, env);
			PropertyConfigurer.setProperty(Constants.KEY_ENV, env);
			PropertyConfigurer.setProperty(Constants.KEY_APP_ENV, env);
		}
		
	}
		
	private void setDeployMeta(String meta) {
		System.setProperty(KEY_DEPLOY_META_URL, meta);
	}
	
	private void setDeployConfigService(String configService) {
		System.setProperty(KEY_DEPLOY_CONFIG_SERVICE, configService);		
	}
		
//	private String setRefreshInterval(String rRefreshInterval) {
//		return System.setProperty(KEY_DEPLOY_REFRESH_INT, rRefreshInterval);
//	}
	
	private void setDeployAppId(String appId) {
		System.setProperty(Constants.KEY_APP_ID, appId);
	}

	@Override
	public void close() {
	}
		
}