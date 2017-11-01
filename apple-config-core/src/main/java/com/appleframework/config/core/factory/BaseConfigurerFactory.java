package com.appleframework.config.core.factory;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.appleframework.config.core.Constants;
import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.event.ConfigListener;
import com.appleframework.config.core.util.ObjectUtils;
import com.appleframework.config.core.util.StringUtils;

public class BaseConfigurerFactory {

	private static Logger logger = Logger.getLogger(BaseConfigurerFactory.class);
	
	protected Collection<ConfigListener> eventListenerSet;
	
	protected Collection<ConfigListener> eventListeners;
    
	protected Collection<String> eventListenerClasss;

	protected String eventListenerClass;

	protected ConfigListener eventListener;

	protected boolean loadRemote = true;
	
	protected boolean isSpringboot = false;
	
	protected boolean remoteFirst = false;
	
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
	
	public boolean isSpringboot() {
		return isSpringboot;
	}

	public void setSpringboot(boolean isSpringboot) {
		this.isSpringboot = isSpringboot;
	}

	public boolean isRemoteFirst() {
		return remoteFirst;
	}

	public void setRemoteFirst(boolean remoteFirst) {
		this.remoteFirst = remoteFirst;
	}

	public void convertLocalProperties(Properties defaultProps) {
		Enumeration<?> propertyNames = defaultProps.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = defaultProps.getProperty(propertyName);
			if (ObjectUtils.isNotEmpty(propertyName)) {
				PropertyConfigurer.setProperty(propertyName, propertyValue);
			}
		}
	}
		
	public void initSystemProperties() {
		// 获取启动启动-D参数
		Properties systemProps = System.getProperties();
		Enumeration<?> systemEnum = systemProps.keys();
		while (systemEnum.hasMoreElements()) {
			String systemKey = systemEnum.nextElement().toString();
			if (!Constants.SET_SYSTEM_PROPERTIES.contains(systemKey)) {
				String systemValue = systemProps.getProperty(systemKey);				
				PropertyConfigurer.setProperty(systemKey, systemValue);
			}
		}
		setSystemProperty(PropertyConfigurer.getProps());
	}

	public void initEventListener() {

		if(null == eventListenerSet) {
			eventListenerSet = new HashSet<ConfigListener>();
		}
					
		// 定义事件源
		
		//1. 处理eventListenerClass
		if (!StringUtils.isNullOrEmpty(eventListenerClass)) {
			try {
				Class<?> clazz = Class.forName(eventListenerClass);
				ConfigListener configListener = (ConfigListener) clazz.newInstance();
				eventListenerSet.add(configListener);
			} catch (Exception e) {
				logger.error(e);
			}
		}

		//2. 处理eventListener
		if (ObjectUtils.isNotEmpty(eventListener)) {
			eventListenerSet.add(eventListener);
		}
		
		
		//3. 处理eventListeners
		if(null != eventListeners) {
			for (ConfigListener eventListenerBean : eventListeners) {
				if (null != eventListenerBean) {
					eventListenerSet.add(eventListenerBean);
				}
			}
		}
		
		//4. 处理eventListenerClasss
		if(null != eventListenerClasss) {
			for (String eventListenerClassStr : eventListenerClasss) {
				try {
					if (!StringUtils.isNullOrEmpty(eventListenerClassStr)) {
						Class<?> clazz = Class.forName(eventListenerClassStr);
						ConfigListener configListener = (ConfigListener) clazz.newInstance();
						eventListenerSet.add(configListener);		
					}
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}

	}
	
	public void setSystemProperty(Properties props) {
		// 讲-D开头的的配置设置到系统变量
		Iterator<Entry<Object, Object>> it = props.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (key.toString().startsWith("-D")) {
				String systemKey = key.toString().trim().substring(2);
				String systemValue = value.toString().trim();
				System.setProperty(systemKey, systemValue);
				logger.warn(key.toString() + "=" + systemValue);
			}
		}
	}

	public void setEventListeners(Collection<ConfigListener> eventListeners) {
		this.eventListeners = eventListeners;
	}

	public void setEventListenerClasss(Collection<String> eventListenerClasss) {
		this.eventListenerClasss = eventListenerClasss;
	}
	
	public Properties getProps() {
		return PropertyConfigurer.getProps();
	}
	
	public void setProperties(Properties properties) {
		PropertyConfigurer.load(properties);
		setSystemProperty(properties);
		notifyPropertiesChanged(properties);
	}
	
	/**
     * 通过listener去通知 reload
     *
     * @param oldProperties
     */
	public void notifyPropertiesChanged(Properties oldProperties) {
		setSystemProperty(oldProperties);
		// 事件触发
		if (eventListenerSet.size() > 0) {
			Iterator<ConfigListener> iterator = eventListenerSet.iterator();
			while (iterator.hasNext()) {
				ConfigListener listener = iterator.next();
				listener.receiveConfigInfo(oldProperties);
			}
		}
	}
	
}