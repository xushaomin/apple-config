package com.appleframework.config.core.spring;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.event.ConfigListener;
import com.appleframework.config.core.factory.ConfigurerFactory;

public class BasePropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	protected Collection<ConfigListener> eventListeners;

	protected Collection<String> eventListenerClasss;

	protected String eventListenerClass;

	protected ConfigListener eventListener;

	protected ConfigurerFactory configurerFactory;

	protected boolean loadRemote = true;

	public boolean isLoadRemote() {
		return loadRemote;
	}

	public void setLoadRemote(boolean loadRemote) {
		this.loadRemote = loadRemote;
	}

	public void setEventListenerClass(String eventListenerClass) {
		this.eventListenerClass = eventListenerClass;
	}

	public void setEventListener(ConfigListener eventListener) {
		this.eventListener = eventListener;
	}

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
	}
	
	@Override
	protected Properties mergeProperties() throws IOException {
		Properties properties = super.mergeProperties();
		PropertyConfigurer.merge(properties);

		// 读取system.properties文件配置
		URL resource = Thread.currentThread().getContextClassLoader().getResource("system.properties");
		if (resource != null) {
			Properties sysConfig = new Properties();
			sysConfig.load(new FileReader(new File(resource.getPath())));
			PropertyConfigurer.merge(sysConfig);
		} else {
			logger.warn("配置文件[system.properties]缺失");
		}
		
		Class<?> clazz;
		try {
			clazz = Class.forName("com.appleframework.config.PropertyConfigurerFactory");
			configurerFactory = (ConfigurerFactory) clazz.newInstance();
		} catch (Exception e) {
			return properties;
		}

		configurerFactory.setLoadRemote(loadRemote);
		configurerFactory.setEventListener(eventListener);
		configurerFactory.setEventListenerClass(eventListenerClass);
		configurerFactory.setEventListenerClasss(eventListenerClasss);
		configurerFactory.setEventListeners(eventListeners);
		configurerFactory.init();

		Properties remoteProperties = configurerFactory.getAllRemoteProperties();
		if (remoteProperties != null) {
			Set<Entry<Object, Object>> entrySet = remoteProperties.entrySet();
			for (Entry<Object, Object> entry : entrySet) {
				// 本地配置优先
				if (configurerFactory.isRemoteFirst() == false && properties.containsKey(entry.getKey())) {
					logger.info("config[" + entry.getKey() + "] exists in location,skip~");
					continue;
				}
				properties.put(entry.getKey(), entry.getValue());
				PropertyConfigurer.add(entry.getKey().toString(), entry.getValue().toString());
			}
		}

		configurerFactory.onLoadFinish(properties);

		return properties;
	}

	public void setEventListeners(Collection<ConfigListener> eventListeners) {
		this.eventListeners = eventListeners;
	}

	public void setEventListenerClasss(Collection<String> eventListenerClasss) {
		this.eventListenerClasss = eventListenerClasss;
	}

}