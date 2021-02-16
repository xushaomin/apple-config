package com.appleframework.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.util.StringUtils;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

/**
* 专门提供apple-boot-java以
* -Dconfig-factory=com.appleframework.config.PropertyConfigurerFactory
* 模式启动
* 
* @author xushaomin
*/
public class NacosConfigurerFactory extends PropertyConfigurerFactory {

	private static Log logger = LogFactory.get(NacosConfigurerFactory.class);

	public NacosConfigurerFactory() {
		
	}
	
	public NacosConfigurerFactory(Properties props) {
		convertLocalProperties(props);
	}
	
	public void init() {
		
		// read system.properties
		URL resource = Thread.currentThread().getContextClassLoader().getResource("system.properties");
		if (resource != null) {
			Properties sysConfig = new Properties();
			try {
				sysConfig.load(new FileReader(new File(resource.getPath())));
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			PropertyConfigurer.merge(sysConfig);
		} else {
			logger.warn("[system.properties] is not exist !");
		}
		
		// read application.properties
		resource = Thread.currentThread().getContextClassLoader().getResource("application.properties");
		if (resource != null) {
			Properties sysConfig = new Properties();
			try {
				sysConfig.load(new FileReader(new File(resource.getPath())));
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			PropertyConfigurer.merge(sysConfig);
		} else {
			logger.warn("[application.properties] is not exist !");
		}
		
		super.init();
		
		String configInfo = getRemoteConfigInfo(null);
		PropertyConfigurer.setConfigInfo(configInfo);
		
		Properties remoteProperties = this.changeToProperties(configInfo);
		if (remoteProperties != null) {
			Set<Entry<Object, Object>> entrySet = remoteProperties.entrySet();
			for (Entry<Object, Object> entry : entrySet) {
				// local configurer first
				if (super.isRemoteFirst() == false && PropertyConfigurer.containsKey(entry.getKey().toString())) {
					logger.info("config[" + entry.getKey() + "] exists in location,skip~");
					continue;
				}
				PropertyConfigurer.add(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		super.onLoadFinish(remoteProperties);
	}
	
	private Properties changeToProperties(String configInfo) {
		Properties properties = new Properties();
		try {
			if (!StringUtils.isEmpty(configInfo)) {
				properties.load(new StringReader(configInfo));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return properties;
	}
	
}