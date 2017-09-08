/**
 * 
 */
package com.appleframework.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.spring.BasePropertyPlaceholderConfigurer;

public class ExtendPropertyPlaceholderConfigurer extends BasePropertyPlaceholderConfigurer implements DisposableBean {

	private final static Logger logger = Logger.getLogger(ExtendPropertyPlaceholderConfigurer.class);
	
	private static boolean isInit = false;

	@Override
	protected Properties mergeProperties() throws IOException {
		Properties properties = super.mergeProperties();
		PropertyConfigurer.merge(properties);
		if(isInit)
			return PropertyConfigurer.getProps();

		// 读取system.properties文件配置
		URL resource = Thread.currentThread().getContextClassLoader().getResource("system.properties");
		if (resource != null) {
			Properties sysConfig = new Properties();
			sysConfig.load(new FileReader(new File(resource.getPath())));
			PropertyConfigurer.merge(sysConfig);
		} else {
			logger.warn("配置文件[system.properties]缺失");
		}
		
		if(null == configurerFactory) {
			configurerFactory = PropertyConfigurerFactory.getInstance();
			configurerFactory.setLoadRemote(loadRemote);
			configurerFactory.setEventListener(eventListener);
			configurerFactory.setEventListenerClass(eventListenerClass);
			configurerFactory.setEventListenerClasss(eventListenerClasss);
			configurerFactory.setEventListeners(eventListeners);
			configurerFactory.init();
		}

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

		isInit = true;
		return properties;
	}

	@Override
	public void destroy() throws Exception {
		configurerFactory.close();
	}

}
